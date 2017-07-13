package io.homeassistant.android.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationResult;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import io.homeassistant.android.Common;
import io.homeassistant.android.HassService;
import io.homeassistant.android.Utils;
import io.homeassistant.android.api.requests.DeviceTrackerRequest;

import static io.homeassistant.android.HassService.EXTRA_ACTION_COMMAND;

public class LocationUpdateReceiver extends BroadcastReceiver implements LostApiClient.ConnectionCallbacks {

    public static final String ACTION_START_LOCATION = "io.homeassistant.location.start";
    public static final String ACTION_LOCATION_UPDATE = "io.homeassistant.location.update";
    private static final String TAG = "LocationUpdater";
    private SharedPreferences prefs;
    private Context tempContext;
    private LostApiClient apiClient;

    @Override
    public void onReceive(Context context, Intent intent) {
        prefs = Utils.getPrefs(context);
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case ACTION_START_LOCATION:
                tempContext = context;
                apiClient = new LostApiClient.Builder(context)
                        .addConnectionCallbacks(this)
                        .build();

                apiClient.connect();
                break;
            case ACTION_LOCATION_UPDATE:
                if (prefs.getBoolean(Common.PREF_ENABLE_LOCATION_TRACKING, false) && LocationResult.hasResult(intent)) {
                    LocationResult result = LocationResult.extractResult(intent);
                    Location location = result.getLastLocation();
                    if (location != null)
                        logLocation(location, context);
                }
                break;
        }
    }

    @Override
    public void onConnected() {
        if (ActivityCompat.checkSelfPermission(tempContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (prefs.getBoolean(Common.PREF_ENABLE_LOCATION_TRACKING, false) && !TextUtils.isEmpty(prefs.getString(Common.PREF_LOCATION_DEVICE_NAME, null))) {
                LocationRequest locationRequest = LocationRequest.create()
                .setInterval(prefs.getInt(Common.PREF_LOCATION_UPDATE_INTERVAL, 10) * 60 * 1000)
                .setFastestInterval(5 * 60 * 1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, getPendingIntent(tempContext));
                Log.d(TAG, "Started requesting location updates");
            } else {
                LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, getPendingIntent(tempContext));
                Log.d(TAG, "Stopped requesting location updates");
            }
        }
        apiClient.disconnect();
        tempContext = null;
    }

    @Override
    public void onConnectionSuspended() {
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_LOCATION_UPDATE);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void logLocation(@NonNull Location location, @NonNull Context context) {
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
        Intent serviceIntent = new Intent(context, HassService.class);
        serviceIntent.putExtra(EXTRA_ACTION_COMMAND, new DeviceTrackerRequest(deviceName, location.getLatitude(), location.getLongitude(), Math.round(location.getAccuracy()), percentage).toString());
        context.startService(serviceIntent);
    }
}