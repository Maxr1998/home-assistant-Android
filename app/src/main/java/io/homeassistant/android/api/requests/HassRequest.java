package io.homeassistant.android.api.requests;

import com.afollestad.ason.Ason;

public abstract class HassRequest {
    protected final String type;

    public HassRequest(String type) {
        this.type = type;
    }

    public Ason toAson() {
        return Ason.serialize(this, true);
    }
}