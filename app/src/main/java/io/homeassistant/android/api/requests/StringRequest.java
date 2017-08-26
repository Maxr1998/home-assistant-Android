package io.homeassistant.android.api.requests;

import com.afollestad.ason.Ason;

public class StringRequest extends HassRequest {
    private final Ason ason;

    public StringRequest(String request) {
        super(null);
        ason = new Ason(request);
    }

    @Override
    public Ason toAson() {
        return ason;
    }
}