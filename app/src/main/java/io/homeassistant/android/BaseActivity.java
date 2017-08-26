package io.homeassistant.android;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;

import io.homeassistant.android.api.requests.HassRequest;
import io.homeassistant.android.api.results.RequestResult;

public abstract class BaseActivity extends AppCompatActivity implements CommunicationHandler.ServiceCommunicator {

    protected final Handler communicationHandler = new CommunicationHandler(this);
    @VisibleForTesting()
    public HassService service;
    protected final ServiceConnection hassConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((HassService.HassBinder) binder).getService();
            service.setActivityHandler(communicationHandler);
            // Make sure that service is connected, if not it'll re-attempt
            service.connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service.setActivityHandler(null);
            service = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, HassService.class), hassConnection, BIND_AUTO_CREATE);
    }

    public void attemptLogin() {
        // Force reconnect, this call is safe if not connected
        service.disconnect();
        service.connect();
    }

    public void send(HassRequest message, @Nullable RequestResult.OnRequestResultListener resultListener) {
        if (!service.send(message, resultListener) && resultListener != null) {
            resultListener.onRequestResult(false, null);
        }
    }

    @Override
    protected void onDestroy() {
        // Keep service alive for configuration changes. No connection will be leaked as it's bound to application context
        if (!isChangingConfigurations()) {
            getApplicationContext().unbindService(hassConnection);
        }
        super.onDestroy();
    }
}