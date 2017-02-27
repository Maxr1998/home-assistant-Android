package io.homeassistant.android;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.EditText;

import com.afollestad.ason.Ason;

import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

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


public class HassActivity extends AppCompatActivity {

    private final Handler postHandler = new Handler();
    private final LoginUtils loginUtils = new LoginUtils();

    private SharedPreferences prefs;
    private WebSocket hassSocket;
    private int lastId = 0;
    private WebSocketListener socketListener = new HassSocketListener();

    private Map<String, Entity> entityMap = new HashMap<>();
    private ViewAdapter viewAdapter = new ViewAdapter(this, entityMap);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hass);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        loginUtils.connect();

        RecyclerView viewRecycler = (RecyclerView) findViewById(R.id.view_recycler);
        viewRecycler.setLayoutManager(new LinearLayoutManager(this));
        viewRecycler.setItemAnimator(new DefaultItemAnimator());
        viewRecycler.setAdapter(viewAdapter);
    }

    public WebSocket getHassSocket() {
        return hassSocket;
    }

    public synchronized int getNewID() {
        return ++lastId;
    }

    @Override
    protected void onDestroy() {
        hassSocket.close(1001, "Application closed");
        super.onDestroy();
    }

    @SuppressLint("InflateParams")
    private class LoginUtils {

        private void connect() {
            OkHttpClient client = new OkHttpClient();

            String ip = prefs.getString(PREF_HASS_URL_KEY, "");
            if (ip.length() > 0 && ip.charAt(ip.length() - 1) == '/') {
                ip = ip.substring(0, ip.length() - 1);
            }
            HttpUrl url = HttpUrl.parse(ip = ip.concat("/api/websocket"));
            Log.d("IP", ip);
            if (url != null) {
                hassSocket = client.newWebSocket(new Request.Builder().url(url).build(), socketListener);
            } else {
                askForIP();
            }
        }

        private void askForIP() {
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_ip, null);
            final EditText input = (EditText) dialogView.findViewById(R.id.ip_input);
            input.setText(prefs.getString(PREF_HASS_URL_KEY, ""));
            new AlertDialog.Builder(HassActivity.this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putString(PREF_HASS_URL_KEY, input.getText().toString()).apply();
                            connect();
                        }
                    })
                    .create()
                    .show();
        }

        private void doAuth() {
            final Runnable sendPassword = new Runnable() {
                @Override
                public void run() {
                    hassSocket.send(new AuthRequest(prefs.getString(Common.PREF_HASS_PASSWORD_KEY, "")).toString());
                }
            };
            if (prefs.contains(Common.PREF_HASS_PASSWORD_KEY)) {
                sendPassword.run();
            } else {
                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_password, null);
                final EditText input = (EditText) dialogView.findViewById(R.id.password_input);
                new AlertDialog.Builder(HassActivity.this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().putString(Common.PREF_HASS_PASSWORD_KEY, input.getText().toString()).apply();
                                sendPassword.run();
                            }
                        })
                        .create()
                        .show();
            }
        }
    }

    private class HassSocketListener extends WebSocketListener {

        private static final String TAG = "Server";

        private static final int TYPE_ERROR = -1;
        private static final int TYPE_STATES = 1;

        private SparseIntArray requests = new SparseIntArray();

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Ason message = new Ason(text);
            //noinspection ConstantConditions
            switch (message.getString("type", "")) {
                case "auth_failed":
                    prefs.edit().remove(Common.PREF_HASS_PASSWORD_KEY).apply();
                case "auth_required":
                    postHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loginUtils.doAuth();
                        }
                    });
                    break;
                case "auth_ok":
                    final int rid = getNewID();
                    requests.append(rid, TYPE_STATES);
                    webSocket.send(new StatesRequest(rid).toString());
                    break;
                case "result":
                    Log.d(TAG, message.toString());
                    RequestResult res = Ason.deserialize(message, RequestResult.class);
                    switch (requests.get(res.id, TYPE_ERROR)) {
                        case TYPE_STATES:
                            if (HassUtils.extractEntitiesFromStateResult(res, entityMap)) {
                                postHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewAdapter.mapUpdated();
                                    }
                                });
                            }
                            break;
                    }
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            if (t instanceof ConnectException || t instanceof ProtocolException || t instanceof UnknownHostException) {
                postHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loginUtils.askForIP();
                    }
                });
                return;
            }
            Log.e(TAG, "Error: " + t.getClass().getName(), t);
        }
    }
}