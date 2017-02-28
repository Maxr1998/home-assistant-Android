package io.homeassistant.android;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.lang.ref.WeakReference;

import static io.homeassistant.android.Common.PREF_HASS_URL_KEY;


public class HassActivity extends AppCompatActivity {

    private final Handler communicationHandler = new CommunicationHandler(this);
    private HassService service;
    private final ServiceConnection hassConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((HassService.HassBinder) binder).getService();
            service.setActivityHandler(communicationHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service.setActivityHandler(null);
            service = null;
        }
    };
    private SharedPreferences prefs;

    private ViewAdapter viewAdapter = new ViewAdapter(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hass);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        bindService(new Intent(this, HassService.class), hassConnection, BIND_AUTO_CREATE);

        RecyclerView viewRecycler = (RecyclerView) findViewById(R.id.view_recycler);
        viewRecycler.setLayoutManager(new LinearLayoutManager(this));
        viewRecycler.setItemAnimator(new DefaultItemAnimator());
        viewRecycler.setAdapter(viewAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (service != null) {
            // Check connection / Reconnect when activity was hidden/inactive for a short time
            service.connect();
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(hassConnection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hass, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if (service != null) {
                    service.loadStates();
                }
                return true;
            default:
                return false;
        }
    }

    private void obtainURL() {
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
                        service.connect();
                    }
                })
                .create()
                .show();
    }

    private void obtainPassword() {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_password, null);
        final EditText input = (EditText) dialogView.findViewById(R.id.password_input);
        new AlertDialog.Builder(HassActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().putString(Common.PREF_HASS_PASSWORD_KEY, input.getText().toString()).apply();
                        service.authenticate();
                    }
                })
                .create()
                .show();
    }

    private void updateStates() {
        viewAdapter.updateEntities(service.getEntityMap());
    }

    public int getNewID() {
        return service.getNewID();
    }

    public boolean send(String message) {
        return service.send(message);
    }

    public static class CommunicationHandler extends Handler {

        public static final int MESSAGE_OBTAIN_URL = 0x04;
        public static final int MESSAGE_OBTAIN_PASSWORD = 0x08;
        public static final int MESSAGE_STATES_AVAILABLE = 0x10;

        private final WeakReference<HassActivity> activity;

        private CommunicationHandler(HassActivity a) {
            activity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            if (activity.get() == null) {
                return;
            }
            switch (msg.what) {
                case MESSAGE_OBTAIN_URL:
                    activity.get().obtainURL();
                    break;
                case MESSAGE_OBTAIN_PASSWORD:
                    activity.get().obtainPassword();
                    break;
                case MESSAGE_STATES_AVAILABLE:
                    activity.get().updateStates();
                    break;
            }
        }
    }
}