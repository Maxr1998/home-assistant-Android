package io.homeassistant.android;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.afollestad.ason.Ason;

import java.io.IOException;
import java.util.List;

import io.homeassistant.android.api.rest.DeviceTrackerData;
import io.homeassistant.android.api.rest.HassRestService;
import io.homeassistant.android.api.websocket.results.Entity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Nicolas on 2017-12-04.
 */

public class LocationUpdateHandler {
    static final String TAG = LocationUpdateHandler.class.getSimpleName();
    private final HassRestService api;
    private final String deviceName;
    private final Context context;

    public LocationUpdateHandler(HassRestService api, String deviceName, Context context) {
        this.api = api;
        this.context = context.getApplicationContext();
        this.deviceName = deviceName;
    }

    public void onLocation(@NonNull Location location)  {
        Log.d(TAG, "Sending location");
        String deviceName = Utils.getPrefs(context).getString(Common.PREF_LOCATION_DEVICE_NAME, null);
        if (TextUtils.isEmpty(deviceName)) {
            return;
        }

        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int percentage = 0;
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
            percentage = Math.round(level / (float) scale * 100);
        }

        DeviceTrackerData body = new DeviceTrackerData();
        body.dev_id = deviceName;
        body.setLocation(location.getLatitude(), location.getLongitude());
        body.gps_accuracy = Math.round(location.getAccuracy());
        body.battery = percentage;


        // use rest api so we don't have to manage a connection
        try {
            api.setDeviceLocation(body).execute();
        } catch (IOException e) {
            Log.e(TAG,"Failed to set location",e);
        }
    }
}
