package io.homeassistant.android.api.results;

import com.afollestad.ason.AsonIgnore;
import com.afollestad.ason.AsonName;

import io.homeassistant.android.api.EntityType;

public class Entity {
    @AsonIgnore
    public EntityType type = EntityType.BASE;

    @AsonName(name = "entity_id")
    public String id;
    public String last_changed;
    public String last_updated;
    public String state = null;
    public Attributes attributes;

    @Override
    public String toString() {
        return String.format("%1$s, %2$s", type, id);
    }
}