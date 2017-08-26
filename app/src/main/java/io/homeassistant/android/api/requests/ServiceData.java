package io.homeassistant.android.api.requests;

import com.afollestad.ason.Ason;

public class ServiceData extends Ason {
    ServiceData(String id) {
        put("entity_id", id);
    }
}