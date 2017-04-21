package io.homeassistant.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.ImageButton;

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
        return getPrefs(context).getStringSet(Common.PREF_ALLOWED_HOST_MISMATCHES_KEY, Collections.emptySet());
    }

    public static void addAllowedHostMismatch(Context context, String allowed) {
        Set<String> set = new HashSet<>(getAllowedHostMismatches(context));
        set.add(allowed);
        getPrefs(context).edit().putStringSet(Common.PREF_ALLOWED_HOST_MISMATCHES_KEY, set).apply();
    }

    /**
     * Sets the specified image button to the given state, while modifying or "graying-out"
     * the icon as well
     *
     * @param enabled   The state of the menu item
     * @param item      The menu item to modify
     * @param iconResId The icon ID
     */
    public static void setStatefulImageButtonIcon(Context c, boolean enabled, ImageButton item, int iconResId) {
        item.setEnabled(enabled);
        Drawable originalIcon = ContextCompat.getDrawable(c, iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);
    }

    /**
     * Mutates and applies a filter that converts the given drawable to a Gray image.
     * This method may be used to simulate the color of disabled icons in Honeycomb's ActionBar.
     *
     * @return a mutated version of the given drawable with a color filter applied.
     */
    private static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }
}
