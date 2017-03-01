package io.homeassistant.android.api.requests;

public class ServiceData {
    protected final String entity_id;

    ServiceData(String id) {
        entity_id = id;
    }
}