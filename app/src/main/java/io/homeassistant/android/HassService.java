package io.homeassistant.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.afollestad.ason.Ason;

import java.lang.ref.SoftReference;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;

import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.requests.AuthRequest;
import io.homeassistant.android.api.requests.StatesRequest;
import io.homeassistant.android.api.requests.SubscribeEventsRequest;
import io.homeassistant.android.api.results.Entity;
import io.homeassistant.android.api.results.EventResult;
import io.homeassistant.android.api.results.RequestResult;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.internal.tls.OkHostnameVerifier;

import static io.homeassistant.android.CommunicationHandler.FAILURE_REASON_GENERIC;
import static io.homeassistant.android.CommunicationHandler.FAILURE_REASON_SSL_MISMATCH;
import static io.homeassistant.android.CommunicationHandler.FAILURE_REASON_WRONG_PASSWORD;
import static io.homeassistant.android.CommunicationHandler.MESSAGE_LOGIN_FAILED;
import static io.homeassistant.android.CommunicationHandler.MESSAGE_LOGIN_SUCCESS;
import static io.homeassistant.android.CommunicationHandler.MESSAGE_STATES_AVAILABLE;

public class HassService extends Service {

    public static final String EXTRA_ACTION_COMMAND = "extra_action_command";

    public static final int AUTH_STATE_NOT_AUTHENTICATED = 0;
    public static final int AUTH_STATE_AUTHENTICATING = 1;
    public static final int AUTH_STATE_AUTHENTICATED = 2;

    private static final String TAG = HassService.class.getSimpleName();
    private final HassBinder binder = new HassBinder();
    private final Map<String, Entity> entityMap = new HashMap<>();
    public AtomicBoolean connecting = new AtomicBoolean(false);
    public AtomicBoolean connected = new AtomicBoolean(false);
    public AtomicInteger authenticationState = new AtomicInteger(AUTH_STATE_NOT_AUTHENTICATED);
    private WebSocket hassSocket;
    private WebSocketListener socketListener = new HassSocketListener();
    private AtomicInteger lastId = new AtomicInteger(0);

    private Handler activityHandler;
    private SparseArray<SoftReference<RequestResult.OnRequestResultListener>> requests = new SparseArray<>(3);

    private Queue<String> actionsQueue = new LinkedList<>();
    private AtomicBoolean handlingQueue = new AtomicBoolean(false);
    private Handler stopServiceHandler = new Handler();

    @Override
    public void onCreate() {
        connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent.getStringExtra(EXTRA_ACTION_COMMAND);
        if (command != null) {
            actionsQueue.add(command);
            if (authenticationState.get() == AUTH_STATE_AUTHENTICATED)
                handleActionsQueue();
            return START_NOT_STICKY;
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        disconnect();
    }

    public void setActivityHandler(Handler handler) {
        activityHandler = handler;
    }

    public void connect() {
        // Don't try to connect if already connecting
        if (!connecting.compareAndSet(false, true))
            return;

        // Check if already connected
        if (hassSocket != null) {
            if (connected.get()) {
                connecting.set(false);
                // Still connected, reload states
                if (activityHandler != null) {
                    loadStates();
                }
                return;
            } else disconnect();
        }
        // Connect to WebSocket
        String url = Utils.getUrl(this);
        // Don't connect if no url or password is set - instances without password have their password set to Common.NO_PASSWORD
        if (!url.isEmpty() && !Utils.getPassword(this).isEmpty()) {
            Log.d("Home Assistant URL", url = url.concat("/api/websocket"));
            OkHttpClient client = new OkHttpClient.Builder()
                    .hostnameVerifier((hostname, session) -> {
                        if (OkHostnameVerifier.INSTANCE.verify(hostname, session) || Utils.getAllowedHostMismatches(HassService.this).contains(hostname)) {
                            return true;
                        }
                        loginMessage(false, FAILURE_REASON_SSL_MISMATCH);
                        return false;
                    }).build();
            hassSocket = client.newWebSocket(new Request.Builder().url(HttpUrl.parse(url)).build(), socketListener);
        } else connecting.set(false);
    }

    private void authenticate() {
        if (!authenticationState.compareAndSet(AUTH_STATE_NOT_AUTHENTICATED, AUTH_STATE_AUTHENTICATING)) {
            return;
        }
        String password = Utils.getPassword(this);
        if (password.length() > 0)
            send(new AuthRequest(password), null);
    }

    private void loginMessage(boolean success, int reason) {
        authenticationState.set(success ? AUTH_STATE_AUTHENTICATED : AUTH_STATE_NOT_AUTHENTICATED);
        if (activityHandler != null) {
            activityHandler.obtainMessage(success ? MESSAGE_LOGIN_SUCCESS : MESSAGE_LOGIN_FAILED, reason, 0).sendToTarget();
        }
    }

    public void subscribeEvents() {
        SubscribeEventsRequest eventSubscribe = new SubscribeEventsRequest("state_changed");
        send(eventSubscribe, (success, result) -> {
            Log.i(TAG, "Subscribed to events");
        });
    }

    public void loadStates() {
        if (authenticationState.get() != AUTH_STATE_AUTHENTICATED) {
            authenticate();
            return;
        }
        send(new StatesRequest(), (success, result) -> {
            if (success && HassUtils.extractEntitiesFromStateResult(result, entityMap)) {
                activityHandler.obtainMessage(MESSAGE_STATES_AVAILABLE).sendToTarget();
            }
        });
    }

    public Map<String, Entity> getEntityMap() {
        return entityMap;
    }

    public boolean send(Ason message, @Nullable RequestResult.OnRequestResultListener resultListener) {
        if (!(message instanceof AuthRequest)) {
            int rId = lastId.incrementAndGet();
            message.put("id", rId);
            if (resultListener != null) {
                requests.append(rId, new SoftReference<>(resultListener));
            }
        }
        return hassSocket != null && hassSocket.send(message.toString());
    }

    private void handleActionsQueue() {
        if (handlingQueue.compareAndSet(false, true)) {
            // Automatically stop the service after 30 seconds, queue should be empty by then and service not needed anymore
            stopServiceHandler.postDelayed(this::stopSelf, 30 * 1000);
            runNextAction();
        }
    }

    private void runNextAction() {
        if (actionsQueue.peek() != null) {
            Log.d(TAG, "Sending action command " + actionsQueue.peek());
            send(new Ason(actionsQueue.remove()), (success, result) -> runNextAction());
        } else handlingQueue.set(false);
    }

    public void disconnect() {
        if (hassSocket != null) {
            hassSocket.close(1001, "Application closed");
            hassSocket = null;
        } else {
            connected.set(false);
        }
        authenticationState.set(AUTH_STATE_NOT_AUTHENTICATED);
    }

    public class HassBinder extends Binder {
        public HassService getService() {
            return HassService.this;
        }
    }

    private class HassSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            connecting.set(false);
            connected.set(true);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                Ason message = new Ason(text);
                String type = message.getString("type", "");
                switch (type != null ? type : "") {
                    case "auth_required":
                        Log.d(TAG, "Authenticating..");
                        authenticate();
                        break;
                    case "auth_failed":
                    case "auth_invalid":
                        Log.w(TAG, "Authentication failed!");
                        loginMessage(false, FAILURE_REASON_WRONG_PASSWORD);
                        break;
                    case "auth_ok":
                        Log.d(TAG, "Authenticated.");
                        loginMessage(true, 0);
                        // Automatically load current states if bound to Activity
                        if (activityHandler != null) {
                            subscribeEvents();
                            loadStates();
                        } else handleActionsQueue();
                        break;
                    case "event":
                        EventResult eventRequest = Ason.deserialize(message, EventResult.class);
                        Entity updated;
                        if ((updated = HassUtils.updateEntityFromEventResult(eventRequest.event.data, entityMap)) != null) {
                            Log.d(TAG, "Updated " + updated.id);
                            activityHandler.post(updated::notifyObservers);
                        }
                        break;
                    case "result":
                        RequestResult res = Ason.deserialize(message, RequestResult.class);
                        Log.d(TAG, String.format(
                                "Request %1$d %2$s\nResult: %3$s\nError : %4$s", res.id, res.success ? "successful" : "failed",
                                res.result != null && res.result.getClass().isArray() ? Arrays.toString((Object[]) res.result) : Objects.toString(res.result),
                                String.valueOf(res.error)
                        ));
                        RequestResult.OnRequestResultListener resultListener = requests.get(res.id, new SoftReference<>(null)).get();
                        if (resultListener != null) {
                            resultListener.onRequestResult(res.success, res.result);
                            requests.remove(res.id);
                        }
                        break;
                }
            } catch (Throwable t) { // Catch everything that it doesn't get passed to onFailure
                Log.e(TAG, "Error in onMessage()", t);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            connected.set(false);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            connecting.set(false);
            connected.set(false);
            if (t instanceof SocketException || t instanceof ProtocolException || t instanceof SSLException || t instanceof UnknownHostException) {
                Log.e(TAG, "Error while connecting to Socket, going to try again: " + t.getClass().getSimpleName());
                disconnect();
                if (!(t instanceof SSLPeerUnverifiedException))
                    loginMessage(false, FAILURE_REASON_GENERIC);
                return;
            }
            Log.e(TAG, "Error from onFailure()", t);
        }
    }
}