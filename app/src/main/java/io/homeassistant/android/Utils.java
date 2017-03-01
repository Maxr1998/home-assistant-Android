package io.homeassistant.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;

public final class Utils {

    private static WeakReference<SharedPreferences> PREFS = new WeakReference<>(null);

    private Utils() {
    }

    public static SharedPreferences getPrefs(Context context) {
        if (PREFS.get() == null) {
            PREFS = new WeakReference<>(PreferenceManager.getDefaultSharedPreferences(context));
        }
        return PREFS.get();
    }

    public static String getUrl(Context context) {
        return getPrefs(context).getString(Common.PREF_HASS_URL_KEY, "");
    }

    public static String getPassword(Context context) {
        return getPrefs(context).getString(Common.PREF_HASS_PASSWORD_KEY, "");
    }
}
