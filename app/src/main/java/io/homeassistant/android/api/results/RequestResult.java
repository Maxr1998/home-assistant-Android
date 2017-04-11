package io.homeassistant.android.api.results;

import com.afollestad.ason.Ason;

public class RequestResult {
    public int id;
    public String type;
    public boolean success;
    public Object result;
    public Ason error;

    public interface OnRequestResultListener {
        void onRequestResult(boolean success, Object result);
    }
}