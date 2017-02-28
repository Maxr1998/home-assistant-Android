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

import static io.homeassistant.android.Common.PREF_HASS_URL_KEY;

public class HassService extends Service {

    private static final int TYPE_ERROR = -1;
    private static final int TYPE_STATES = 1;

    private final HassBinder binder = new HassBinder();
    private final Map<String, Entity> entityMap = new HashMap<>();

    private SharedPreferences prefs;
    private WebSocket hassSocket;
    private WebSocketListener socketListener = new HassSocketListener();
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
        hassSocket.close(1001, "Application closed");
    }

    public void setActivityHandler(Handler handler) {
        activityHandler = handler;
        loadStates();
    }

    public void connect() {
        // Check if already connected
        if (hassSocket != null) {
            if (!hassSocket.send("")) {
                hassSocket.cancel();
            } else return;
        }
        // Connect to WebSocket
        OkHttpClient client = new OkHttpClient();
        String url = prefs.getString(PREF_HASS_URL_KEY, "");
        if (url.length() > 0) {
            if (url.charAt(url.length() - 1) == '/') {
                url = url.substring(0, url.length() - 1);
            }
            HttpUrl httpUrl = HttpUrl.parse(url = url.concat("/api/websocket"));
            Log.d("Home Assistant URL", url);
            if (httpUrl != null) {
                hassSocket = client.newWebSocket(new Request.Builder().url(httpUrl).build(), socketListener);
            } else askForURL();
        } else askForURL();
    }

    public void authenticate() {
        send(new AuthRequest(prefs.getString(Common.PREF_HASS_PASSWORD_KEY, "")).toString());
    }

    private void askForURL() {
        // Ask Activity to obtain Hass URL if bound to one
        if (activityHandler != null) {
            activityHandler.obtainMessage(HassActivity.CommunicationHandler.MESSAGE_OBTAIN_URL).sendToTarget();
        }
    }

    private void askForPassword() {
        // Ask Activity to obtain password if bound to one
        if (activityHandler != null) {
            activityHandler.obtainMessage(HassActivity.CommunicationHandler.MESSAGE_OBTAIN_PASSWORD).sendToTarget();
        }
    }

    public int getNewID() {
        return lastId.incrementAndGet();
    }

    public void loadStates() {
        if (!authenticated) {
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

        private static final String TAG = "Server";

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Ason message = new Ason(text);
            //noinspection ConstantConditions
            switch (message.getString("type", "")) {
                case "auth_required":
                    authenticate();
                    break;
                case "auth_failed":
                    askForPassword();
                    break;
                case "auth_ok":
                    authenticated = true;
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
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            if (t instanceof ConnectException || t instanceof ProtocolException || t instanceof UnknownHostException) {
                askForURL();
                return;
            }
            Log.e(TAG, "Error: " + t.getClass().getName(), t);
        }
    }
}