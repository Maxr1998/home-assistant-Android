package io.homeassistant.android;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseIntArray;

import com.afollestad.ason.Ason;

import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLException;

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

import static io.homeassistant.android.HassActivity.CommunicationHandler.MESSAGE_LOGIN_FAILED;
import static io.homeassistant.android.HassActivity.CommunicationHandler.MESSAGE_LOGIN_SUCCESS;

public class HassService extends Service {

    private static final String TAG = HassService.class.getSimpleName();

    private static final int TYPE_ERROR = -1;
    private static final int TYPE_STATES = 1;

    private final HassBinder binder = new HassBinder();
    private final Map<String, Entity> entityMap = new HashMap<>();

    private SharedPreferences prefs;
    private WebSocket hassSocket;
    private WebSocketListener socketListener = new HassSocketListener();
    private boolean connected = false;
    private boolean authenticated = false;
    private AtomicInteger lastId = new AtomicInteger(0);

    private Handler activityHandler;
    private SparseIntArray requests = new SparseIntArray();

    @Override
    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        connect();
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
                if (url.charAt(url.length() - 1) == '/') {
                    url = url.substring(0, url.length() - 1);
                }
                HttpUrl httpUrl = HttpUrl.parse(url = url.concat("/api/websocket"));
                Log.d("Home Assistant URL", url);
                if (httpUrl != null) {
                    OkHttpClient client = new OkHttpClient();
                    hassSocket = client.newWebSocket(new Request.Builder().url(httpUrl).build(), socketListener);
                    connected = true;
                } else {
                    loginMessage(false);
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
                send(new AuthRequest(password).toString());
        }
    }

    private void loginMessage(boolean success) {
        authenticated = success;
        if (activityHandler != null) {
            activityHandler.obtainMessage(success ? MESSAGE_LOGIN_SUCCESS : MESSAGE_LOGIN_FAILED).sendToTarget();
        }
    }

    public int getNewID() {
        return lastId.incrementAndGet();
    }

    public void loadStates() {
        if (!authenticated) {
            authenticate();
            return;
        }
        final int rid = getNewID();
        requests.append(rid, TYPE_STATES);
        send(new StatesRequest(rid).toString());
    }

    public Map<String, Entity> getEntityMap() {
        return entityMap;
    }

    public boolean send(String message) {
        return hassSocket != null && hassSocket.send(message);
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
                Log.d(TAG, text);
                Ason message = new Ason(text);
                //noinspection ConstantConditions
                switch (message.getString("type", "")) {
                    case "auth_required":
                        authenticate();
                        break;
                    case "auth_failed":
                        connected = false;
                        loginMessage(false);
                        break;
                    case "auth_ok":
                        loginMessage(true);
                        // Automatically load current states if bound to Activity
                        if (activityHandler != null) {
                            loadStates();
                        }
                        break;
                    case "result":
                        Log.d(TAG, message.toString());
                        RequestResult res = Ason.deserialize(message, RequestResult.class);
                        switch (requests.get(res.id, TYPE_ERROR)) {
                            case TYPE_STATES:
                                if (HassUtils.extractEntitiesFromStateResult(res, entityMap)) {
                                    activityHandler.obtainMessage(HassActivity.CommunicationHandler.MESSAGE_STATES_AVAILABLE).sendToTarget();
                                }
                                break;
                        }
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
                loginMessage(false);
                return;
            }
            Log.e(TAG, "Error in onFailure()", t);
        }
    }
}