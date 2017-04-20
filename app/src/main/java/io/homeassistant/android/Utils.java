package io.homeassistant.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    public static Set<String> getAllowedHostMismatches(Context context) {
        return getPrefs(context).getStringSet(Common.PREF_ALLOWED_HOST_MISMATCHES_KEY, Collections.<String>emptySet());
    }

    public static void addAllowedHostMismatch(Context context, String allowed) {
        Set<String> set = new HashSet<>(getAllowedHostMismatches(context));
        set.add(allowed);
        getPrefs(context).edit().putStringSet(Common.PREF_ALLOWED_HOST_MISMATCHES_KEY, set).apply();
    }
}
