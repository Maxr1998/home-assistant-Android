package io.homeassistant.android;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Nicolas on 2017-11-30.
 */

public class HassApp extends Application {
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);
    }
}
