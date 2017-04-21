package io.homeassistant.android.api.requests;

import com.afollestad.ason.Ason;

public class SubscribeEventsRequest extends Ason {

    protected final String type = "subscribe_events";
    protected String event_type = null;

    public SubscribeEventsRequest(String eventType) {
        this.event_type = eventType;
    }
}
