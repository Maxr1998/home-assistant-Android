package io.homeassistant.android.api.requests;

import com.afollestad.ason.Ason;

public class DeviceTrackerRequest extends Ason {

    protected final String type = "call_service";
    protected final String domain = "device_tracker";
    protected final String service = "see";

    public DeviceTrackerRequest(String id, double latitude, double longitude, int accuracy, int batteryLevel) {
        toString();
        put("service_data.dev_id", id);
        put("service_data.gps", latitude, longitude);
        put("service_data.gps_accuracy", accuracy);
        put("service_data.battery", batteryLevel);
    }
}