package io.homeassistant.android.api.websocket.requests;

public class StatesRequest extends HassRequest {
    public StatesRequest() {
        super("get_states");
    }
}