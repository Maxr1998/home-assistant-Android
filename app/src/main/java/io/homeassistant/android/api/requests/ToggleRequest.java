package io.homeassistant.android.api.requests;

import com.afollestad.ason.Ason;

import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.results.Entity;

public class ToggleRequest extends Ason {
    protected final int id;
    protected final String type = "call_service";
    protected final String domain;
    protected final String service;
    protected final Object service_data;

    /**
     * For use with lights (without changing brightness), switches
     */
    public ToggleRequest(int id, Entity entity, boolean state) {
        this.id = id;
        domain = HassUtils.extractDomainFromEntityId(entity.id);
        service = state ? "turn_on" : "turn_off";
        service_data = new ServiceData(entity.id);
    }

    /**
     * For use with lights supporting brightness
     */
    public ToggleRequest(int id, Entity entity, int brightness) {
        this(id, entity);
        toString();
        put("service_data.brightness", brightness);
    }

    /**
     * For use with e.g. scenes
     */
    public ToggleRequest(int id, Entity entity) {
        this(id, entity, true);
    }
}