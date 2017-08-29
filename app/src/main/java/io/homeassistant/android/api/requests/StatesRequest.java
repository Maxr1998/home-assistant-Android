package io.homeassistant.android.api.requests;

public class StatesRequest extends HassRequest {
    public StatesRequest() {
        super("get_states");
    }
}