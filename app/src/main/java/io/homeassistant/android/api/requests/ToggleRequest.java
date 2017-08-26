package io.homeassistant.android.api.requests;

import com.afollestad.ason.AsonIgnore;

import io.homeassistant.android.api.results.Entity;

import static io.homeassistant.android.api.Domain.LOCK;

public class ToggleRequest extends ServiceRequest {
    @AsonIgnore
    private byte tmp;

    /**
     * For use with lights (without changing brightness), locks, switches
     */
    public ToggleRequest(Entity entity, boolean state) {
        this(entity, entity.getDomain().equals(LOCK) ? (state ? "lock" : "unlock") : (state ? "turn_on" : "turn_off"));
    }

    /**
     * For use with cover
     */
    public ToggleRequest(Entity entity, String operation) {
        super(entity.getDomain(), operation, entity.id);
    }

    /**
     * For use with lights supporting brightness
     */
    public ToggleRequest(Entity entity, int brightness) {
        this(entity);
        data.put("brightness", brightness);
    }

    /**
     * For use with e.g. scenes
     */
    public ToggleRequest(Entity entity) {
        this(entity, true);
    }
}