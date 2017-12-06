package io.homeassistant.android.api.websocket.results;

public class Event {
    public int id;
    public String event_type;
    public String origin;
    public String time_fired;
    public EventData data;
}