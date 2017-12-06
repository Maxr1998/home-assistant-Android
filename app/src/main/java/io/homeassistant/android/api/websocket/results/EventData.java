package io.homeassistant.android.api.websocket.results;

public class EventData {
    public String entity_id;
    public Entity new_state;
    public Entity old_state;
}