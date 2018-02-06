package io.homeassistant.android

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.widget.ImageButton
import okhttp3.Credentials
import java.lang.ref.WeakReference

fun SharedPreferences.getAllowedSSLCerts(): Set<String> {
    return getStringSet(Common.PREF_ALLOWED_INVALID_SSL_CERTS_KEY, emptySet<String>())
}

fun SharedPreferences.addAllowedSSLCert(allowed: String) {
    edit().putStringSet(Common.PREF_ALLOWED_INVALID_SSL_CERTS_KEY, HashSet(getAllowedSSLCerts()).apply {
        add(allowed)
    }).apply()
}

object Utils {

    private var PREFS = WeakReference<SharedPreferences>(null)

    @JvmStatic fun getPrefs(context: Context): SharedPreferences {
        if (PREFS.get() == null) {
            PREFS = WeakReference(PreferenceManager.getDefaultSharedPreferences(context))
        }
        return PREFS.get()!!
    }

    @JvmStatic fun getUrl(context: Context): String {
        return getPrefs(context).getString(Common.PREF_HASS_URL_KEY, "")
    }

    @JvmStatic fun getPassword(context: Context): String {
        return getPrefs(context).getString(Common.PREF_HASS_PASSWORD_KEY, "")
    }

    fun getBasicAuth(context: Context): String {
        return getPrefs(context).getString(Common.PREF_BASIC_AUTH_KEY, "")
    }

    @JvmStatic fun setBasicAuth(context: Context, username: String, password: String) {
        getPrefs(context).edit().putString(Common.PREF_BASIC_AUTH_KEY, Credentials.basic(username, password)).apply()
    }

    fun getAllowedHostMismatches(context: Context): Set<String> {
        return getPrefs(context).getStringSet(Common.PREF_ALLOWED_HOST_MISMATCHES_KEY, emptySet<String>())
    }

    @JvmStatic fun addAllowedHostMismatch(context: Context, allowed: String) {
        getPrefs(context).edit().putStringSet(Common.PREF_ALLOWED_HOST_MISMATCHES_KEY,
                HashSet(getAllowedHostMismatches(context)).apply { add(allowed) }).apply()
    }

    /**
     * Sets the specified image button to the given state, while modifying or "graying-out"
     * the icon as well

     * @param enabled   The state of the menu item
     * *
     * @param item      The menu item to modify
     * *
     * @param iconResId The icon ID
     */
    @JvmStatic fun setStatefulImageButtonIcon(c: Context, enabled: Boolean, item: ImageButton, iconResId: Int) {
        item.isEnabled = enabled
        val originalIcon = ContextCompat.getDrawable(c, iconResId)
        item.setImageDrawable(originalIcon.takeIf { enabled } ?: convertDrawableToGrayScale(originalIcon))
    }

    /**
     * Mutates and applies a filter that converts the given drawable to a Gray image.
     * This method may be used to simulate the color of disabled icons in Honeycomb's ActionBar.

     * @return a mutated version of the given drawable with a color filter applied.
     */
    private fun convertDrawableToGrayScale(drawable: Drawable?): Drawable? {
        return drawable?.mutate()?.apply { setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN) }
    }
}