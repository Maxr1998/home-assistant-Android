package io.homeassistant.android.api.rest;

/**
 * Created by Nicolas on 2017-12-05.
 */

public class DeviceTrackerData {
    public String dev_id;
    public double[] gps = new double[2];
    public double gps_accuracy;
    public int battery;

    public void setLocation(double latitude, double longitude) {
        gps[0] = latitude;
        gps[1] = longitude;
    }
}
