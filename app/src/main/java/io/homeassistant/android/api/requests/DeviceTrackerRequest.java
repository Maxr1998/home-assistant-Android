package io.homeassistant.android.api.requests;

import com.afollestad.ason.AsonIgnore;

public class DeviceTrackerRequest extends ServiceRequest {
    @AsonIgnore
    private byte tmp;

    public DeviceTrackerRequest(String id, double latitude, double longitude, int accuracy, int batteryLevel) {
        super("device_tracker", "see", null);
        data
                .put("dev_id", id)
                .put("gps", latitude, longitude)
                .put("gps_accuracy", accuracy)
                .put("battery", batteryLevel);
    }
}