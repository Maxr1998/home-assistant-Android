package io.homeassistant.android.api.requests;

import com.afollestad.ason.AsonIgnore;

public class StatesRequest extends HassRequest {
    @AsonIgnore
    private byte tmp;

    public StatesRequest() {
        super("get_states");
    }
}