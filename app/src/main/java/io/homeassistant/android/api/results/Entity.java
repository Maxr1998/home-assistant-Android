package io.homeassistant.android.api.results;

import android.support.annotation.NonNull;

import com.afollestad.ason.AsonIgnore;
import com.afollestad.ason.AsonName;

import io.homeassistant.android.api.EntityType;

public class Entity implements Comparable<Entity> {
    @AsonIgnore
    public EntityType type = EntityType.BASE;

    @AsonName(name = "entity_id")
    public String id;
    public String last_changed;
    public String last_updated;
    public String state = null;
    public Attributes attributes;

    public static String getDomain(@NonNull String id) {
        return id.split("\\.")[0];
    }

    @Override
    public String toString() {
        return String.format("%1$s[%2$s]", type, id);
    }

    @Override
    public int compareTo(@NonNull Entity e) {
        if (attributes.order != -1 && e.attributes.order != -1) {
            return attributes.order - e.attributes.order;
        } else if (attributes.friendly_name != null && e.attributes.friendly_name != null) {
            return attributes.friendly_name.compareToIgnoreCase(e.attributes.friendly_name);
        }
        return id.compareToIgnoreCase(e.id);
    }

    public String getDomain() {
        return id.split("\\.")[0];
    }
}