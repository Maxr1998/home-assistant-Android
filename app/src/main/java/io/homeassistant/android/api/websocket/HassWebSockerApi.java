package io.homeassistant.android.api.websocket;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;
import android.util.SparseArray;

import com.afollestad.ason.Ason;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.homeassistant.android.api.websocket.requests.AuthRequest;
import io.homeassistant.android.api.websocket.requests.HassRequest;
import io.homeassistant.android.api.websocket.results.EventResult;
import io.homeassistant.android.api.websocket.results.RequestResult;
import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.internal.tls.OkHostnameVerifier;
import okio.ByteString;


/**
 * Created by Nicolas on 2017-11-21.
 */

public class HassWebSockerApi {

    static final String TAG = HassWebSockerApi.class.getSimpleName();
    public static final int AUTH_STATE_NOT_AUTHENTICATED = 0;
    public static final int AUTH_STATE_AUTHENTICATING = 1;
    public static final int AUTH_STATE_AUTHENTICATED = 2;

    public static final int MESSAGE_LOGIN_SUCCESS = 0x04;
    public static final int MESSAGE_LOGIN_FAILED = 0x08;
    public static final int MESSAGE_STATES_AVAILABLE = 0x10;

    public static final int FAILURE_REASON_GENERIC = 0x1;
    public static final int FAILURE_REASON_WRONG_PASSWORD = 0x2;
    public static final int FAILURE_REASON_BASIC_AUTH = 0x4;
    public static final int FAILURE_REASON_SSL_MISMATCH = 0x8;



    private AtomicBoolean connecting = new AtomicBoolean(false);
    private AtomicBoolean connectionComplete = new AtomicBoolean(false);
    private AtomicInteger lastId = new AtomicInteger(0);
    private AtomicInteger authenticationState = new AtomicInteger(AUTH_STATE_NOT_AUTHENTICATED);

    private SparseArray<MutableLiveData<RequestResult>> requestHandler = new SparseArray<>(3);

    private WebSocket webSocket;
    private String password;


    public static class AuthStatus
    {
        final public boolean sucess;
        final public int reason;

        AuthStatus(boolean sucess, int reason) {
            this.sucess = sucess;
            this.reason = reason;
        }
    }

    private MutableLiveData<Boolean> connected = new MutableLiveData<>();
    private MutableLiveData<EventResult> event = new MutableLiveData<>();
    private MutableLiveData<AuthStatus> authStatus = new MutableLiveData<>();

    private class HassSocketListener extends WebSocketListener
    {
        public HassSocketListener() {
            super();
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            connecting.set(false);
            connectionComplete.set(true);

            connected.postValue(true);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Ason message = new Ason(text);
            String type = message.getString("type", "");
            switch (type)
            {
                case "auth_required":
                    Log.d(TAG, "Authenticating..");
                    authenticate();
                    break;
                case "auth_failed":
                case "auth_invalid":
                    Log.w(TAG, "Authentication failed!");
                    setAuthStatus(false, FAILURE_REASON_WRONG_PASSWORD);
                    break;
                case "auth_ok":
                    Log.d(TAG, "Authenticated.");
                    setAuthStatus(true, 0);
                    break;
                case "event":
                    Log.d(TAG, "Event: " + text);
                    EventResult eventRequest = Ason.deserialize(message, EventResult.class);
                    handleEvent(eventRequest);
                    break;
                case "result":
                    Log.d(TAG, "Result: " + text);
                    RequestResult res = Ason.deserialize(message, RequestResult.class);
                    handleResult(res);
                    break;
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
        }
    }

    private WebSocketListener webSocketListener = new HassSocketListener();


    public HassWebSockerApi()
    {

    }

    public LiveData<Boolean> getConnected() {
        return connected;
    }

    public LiveData<EventResult> getEvent() {
        return event;
    }

    public LiveData<AuthStatus> getAuthStatus() {
        return authStatus;
    }

    public void connect(String baseUrl, String password)
    {
        // Don't try to connect if already connecting
        if (!connecting.compareAndSet(false, true))
            return;

        this.password = password;
        String url = baseUrl + "/api/websocket";
        Log.d(TAG, "url:" + url);

        // TODO: inject OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        if (OkHostnameVerifier.INSTANCE.verify(hostname, session) || getAllowedHostMismatchesFor(hostname)) {
                            return true;
                        }
                        else {
                            setAuthStatus(false, FAILURE_REASON_SSL_MISMATCH);
                            return false;
                        }
                    }
                })
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            setAuthStatus(false, FAILURE_REASON_BASIC_AUTH);
                        }
                        return null;
                    }
                })
                .addNetworkInterceptor( new StethoInterceptor())
                .build();

        Request.Builder requestBuilder = new Request.Builder().url(HttpUrl.parse(url));

        // TODO auth stuff

        webSocket = client.newWebSocket(requestBuilder.build(), webSocketListener);

    }

    public void disconnect() {
        if(webSocket!=null) {
            webSocket.close(1001, "Application closed");
            connectionComplete.set(false);
            connected.postValue(false);
            webSocket = null;
            authenticationState.set(AUTH_STATE_NOT_AUTHENTICATED);
            authStatus.postValue(new AuthStatus(false,AUTH_STATE_NOT_AUTHENTICATED));
        }
    }

    public LiveData<RequestResult>  send(HassRequest request) {

        if(webSocket==null)
            return null;

        Ason message = request.toAson();

        int rId = lastId.incrementAndGet();
        message.put("id", rId);

        final MutableLiveData<RequestResult> liveData = new MutableLiveData<>();

        requestHandler.append(rId, liveData);

        webSocket.send(message.toString());

        return liveData;
    }

    private boolean getAllowedHostMismatchesFor(String hostname) {
        // TODO
        return false;
    }

    private void handleResult(RequestResult res) {

        MutableLiveData<RequestResult> listener = requestHandler.get(res.id);
        if(listener != null)
        {
            listener.postValue(res);
        }

        requestHandler.remove(res.id);

    }

    private void handleEvent(EventResult eventResult) {
        event.postValue(eventResult);
    }

    private void setAuthStatus(boolean success, int reason) {
        authStatus.postValue(new AuthStatus(success,reason));
    }

    private void authenticate() {
        if (!authenticationState.compareAndSet(AUTH_STATE_NOT_AUTHENTICATED, AUTH_STATE_AUTHENTICATING)) {
            return;
        }

        if (password != null && !password.isEmpty()) {
            webSocket.send(new AuthRequest(password).toString());
        }
    }
}
