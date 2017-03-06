package io.homeassistant.android.api.results;

public class RequestResult {
    public int id;
    public String type;
    public boolean success;
    public Object result;

    public interface OnRequestResultListener {
        void onRequestResult(boolean success, Object result);
    }
}