package io.homeassistant.android.api.requests;

import io.homeassistant.android.api.results.Entity;

public class SelectRequest extends ServiceRequest {
    public SelectRequest(Entity entity, String option) {
        super(entity.getDomain(), "select_option", entity.id);
        data.put("option", option);
    }
}