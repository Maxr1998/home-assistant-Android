package io.homeassistant.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import com.afollestad.ason.Ason;

import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.requests.AuthRequest;
import io.homeassistant.android.api.requests.StatesRequest;
import io.homeassistant.android.api.results.Entity;
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

    private static final String TAG = HassService.class.getSimpleName();
    private final HassBinder binder = new HassBinder();
    private final Map<String, Entity> entityMap = new HashMap<>();

    private WebSocket hassSocket;
    private WebSocketListener socketListener = new HassSocketListener();
    private boolean connected = false;
    private boolean authenticated = false;
    private AtomicInteger lastId = new AtomicInteger(0);

    private Handler activityHandler;
    private SparseArray<SoftReference<RequestResult.OnRequestResultListener>> requests = new SparseArray<>(3);

    private Queue<String> actionsQueue = new LinkedList<>();

    @Override
    public void onCreate() {
        connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent.getStringExtra(EXTRA_ACTION_COMMAND);
        if (command != null) {
            actionsQueue.add(command);
            if (authenticated)
                handleActionsQueue();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        if (hassSocket != null)
            hassSocket.close(1001, "Application closed");
    }

    public void setActivityHandler(Handler handler) {
        activityHandler = handler;
    }

    public void connect() {
        synchronized (this) {
            // Check if already connected
            if (hassSocket != null) {
                if (connected) {
                    // Still connected, reload states
                    if (activityHandler != null) {
                        loadStates();
                    }
                    return;
                } else hassSocket.close(1001, "Application reconnect");
            }
            // Connect to WebSocket
            String url = Utils.getUrl(this);
            if (!url.isEmpty()) {
                HttpUrl httpUrl = HttpUrl.parse(url = url.concat("/api/websocket"));
                Log.d("Home Assistant URL", url);
                if (httpUrl != null) {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    Log.d(TAG, hostname);
                                    if (OkHostnameVerifier.INSTANCE.verify(hostname, session) || Utils.getAllowedHostMismatches(HassService.this).contains(hostname)) {
                                        return true;
                                    }
                                    loginMessage(false, FAILURE_REASON_SSL_MISMATCH);
                                    return false;
                                }
                            })
                            .build();
                    hassSocket = client.newWebSocket(new Request.Builder().url(httpUrl).build(), socketListener);
                    connected = true;
                } else {
                    loginMessage(false, FAILURE_REASON_GENERIC);
                }
            }
        }
    }

    private void authenticate() {
        synchronized (this) {
            if (authenticated) {
                return;
            }
            authenticated = true;
            String password = Utils.getPassword(this);
            if (password.length() > 0)
                send(new AuthRequest(password), null);
        }
    }

    private void loginMessage(boolean success, int reason) {
        authenticated = success;
        if (activityHandler != null) {
            activityHandler.obtainMessage(success ? MESSAGE_LOGIN_SUCCESS : MESSAGE_LOGIN_FAILED, reason, 0).sendToTarget();
        }
    }

    private int getNewID() {
        return lastId.incrementAndGet();
    }

    public void loadStates() {
        if (!authenticated) {
            authenticate();
            return;
        }
        send(new StatesRequest(), new RequestResult.OnRequestResultListener() {
            @Override
            public void onRequestResult(boolean success, Object result) {
                if (success && HassUtils.extractEntitiesFromStateResult(result, entityMap)) {
                    activityHandler.obtainMessage(MESSAGE_STATES_AVAILABLE).sendToTarget();
                }
            }
        });
    }

    public Map<String, Entity> getEntityMap() {
        return entityMap;
    }

    public boolean send(Ason message, RequestResult.OnRequestResultListener resultListener) {
        if (!(message instanceof AuthRequest)) {
            int rId = getNewID();
            message.put("id", rId);
            if (resultListener != null) {
                requests.append(rId, new SoftReference<>(resultListener));
            }
        }
        return hassSocket != null && hassSocket.send(message.toString());
    }

    private void handleActionsQueue() {
        if (actionsQueue.peek() != null) {
            send(new Ason(actionsQueue.remove()), new RequestResult.OnRequestResultListener() {
                @Override
                public void onRequestResult(boolean success, Object result) {
                    handleActionsQueue();
                }
            });
        }
    }

    public class HassBinder extends Binder {
        public HassService getService() {
            return HassService.this;
        }
    }

    private class HassSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            connected = true;
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
                        Log.d(TAG, "Authentication failed!");
                        connected = false;
                        loginMessage(false, FAILURE_REASON_WRONG_PASSWORD);
                        break;
                    case "auth_ok":
                        Log.d(TAG, "Authenticated.");
                        loginMessage(true, 0);
                        // Automatically load current states if bound to Activity
                        if (activityHandler != null) {
                            loadStates();
                        } else handleActionsQueue();
                        break;
                    case "result":
                        RequestResult res = Ason.deserialize(message, RequestResult.class);
                        Log.d(TAG, res.id + ": " + message.toString());
                        RequestResult.OnRequestResultListener resultListener = requests.get(res.id, new SoftReference<RequestResult.OnRequestResultListener>(null)).get();
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
            connected = false;
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            connected = false;
            if (t instanceof ConnectException || t instanceof ProtocolException || t instanceof SSLException || t instanceof UnknownHostException) {
                Log.e(TAG, "Error while connecting to Socket, going to try again: " + t.getClass().getSimpleName());
                if (hassSocket != null) {
                    hassSocket.close(1001, "Application error");
                    hassSocket = null;
                }
                if (!(t instanceof SSLPeerUnverifiedException))
                    loginMessage(false, FAILURE_REASON_GENERIC);
                return;
            }
            Log.e(TAG, "Error in onFailure()", t);
        }
    }
}