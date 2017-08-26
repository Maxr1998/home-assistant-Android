package io.homeassistant.android.api.requests;

import com.afollestad.ason.AsonIgnore;

import io.homeassistant.android.api.results.Entity;

public class SelectRequest extends ServiceRequest {
    @AsonIgnore
    private byte tmp;

    public SelectRequest(Entity entity, String option) {
        super(entity.getDomain(), "select_option", entity.id);
        data.put("option", option);
    }
}