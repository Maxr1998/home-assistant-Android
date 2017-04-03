package io.homeassistant.android;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class CommunicationHandler extends Handler {

    public static final int MESSAGE_LOGIN_SUCCESS = 0x04;
    public static final int MESSAGE_LOGIN_FAILED = 0x08;
    public static final int MESSAGE_STATES_AVAILABLE = 0x10;

    public static final int FAILURE_REASON_GENERIC = 0x1;
    public static final int FAILURE_REASON_WRONG_PASSWORD = 0x2;
    public static final int FAILURE_REASON_SSL_MISMATCH = 0x4;

    private final WeakReference<ServiceCommunicator> activity;

    public CommunicationHandler(ServiceCommunicator a) {
        activity = new WeakReference<>(a);
    }

    @Override
    public void handleMessage(Message msg) {
        if (activity.get() == null) {
            return;
        }
        switch (msg.what) {
            case MESSAGE_LOGIN_SUCCESS:
                activity.get().loginSuccess();
                break;
            case MESSAGE_LOGIN_FAILED:
                activity.get().loginFailed(msg.arg1);
                break;
            case MESSAGE_STATES_AVAILABLE:
                activity.get().updateStates();
                break;
        }
    }

    public interface ServiceCommunicator {
        void loginSuccess();

        void loginFailed(int reason);

        void updateStates();
    }
}