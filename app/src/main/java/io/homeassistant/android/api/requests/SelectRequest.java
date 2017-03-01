package io.homeassistant.android.api.requests;

import com.afollestad.ason.Ason;

import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.results.Entity;

public class SelectRequest extends Ason {
    protected final int id;
    protected final String type = "call_service";
    protected final String domain;
    protected final String service = "select_option";
    protected final Object service_data;

    public SelectRequest(int id, Entity entity, String option) {
        this.id = id;
        domain = HassUtils.extractDomainFromEntityId(entity.id);
        service_data = new ServiceData(entity.id);
        toString();
        put("service_data.option", option);
    }
}