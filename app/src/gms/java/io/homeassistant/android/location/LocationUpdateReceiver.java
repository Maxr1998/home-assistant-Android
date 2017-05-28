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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import io.homeassistant.android.Common;
import io.homeassistant.android.HassService;
import io.homeassistant.android.Utils;
import io.homeassistant.android.api.requests.DeviceTrackerRequest;

import static io.homeassistant.android.HassService.EXTRA_ACTION_COMMAND;

public class LocationUpdateReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTION_START_LOCATION = "io.homeassistant.location.start";
    public static final String ACTION_LOCATION_UPDATE = "io.homeassistant.location.update";
    private static final String TAG = "LocationUpdater";
    private SharedPreferences prefs;
    private GoogleApiClient apiClient;

    @Override
    public void onReceive(Context context, Intent intent) {
        prefs = Utils.getPrefs(context);
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case ACTION_START_LOCATION:
                apiClient = new GoogleApiClient.Builder(context)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
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
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(apiClient.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (prefs.getBoolean(Common.PREF_ENABLE_LOCATION_TRACKING, false) && !TextUtils.isEmpty(prefs.getString(Common.PREF_LOCATION_DEVICE_NAME, null))) {
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(prefs.getInt(Common.PREF_LOCATION_UPDATE_INTERVAL, 10) * 60 * 1000);
                locationRequest.setFastestInterval(5 * 60 * 1000);
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, getPendingIntent(apiClient.getContext()));
                Log.d(TAG, "Started requesting location updates");
            } else {
                LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, getPendingIntent(apiClient.getContext()));
                Log.d(TAG, "Stopped requesting location updates");
            }
        }
        apiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(apiClient.getContext(), "Failure: " + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
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