package io.homeassistant.android.api.requests;

public class SubscribeEventsRequest extends HassRequest {
    protected final String event_type;

    public SubscribeEventsRequest(String e) {
        super("subscribe_events");
        event_type = e;
    }
}