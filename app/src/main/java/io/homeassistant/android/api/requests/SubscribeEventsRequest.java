package io.homeassistant.android.api.requests;

import com.afollestad.ason.Ason;

public class SubscribeEventsRequest extends Ason {
    protected final String type = "subscribe_events";
    protected final String event_type;

    public SubscribeEventsRequest(String e) {
        event_type = e;
    }
}