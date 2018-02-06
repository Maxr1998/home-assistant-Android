package io.homeassistant.android;

public final class Common {

    public static final String PREF_HASS_URL_KEY = "hass_url";
    public static final String PREF_HASS_PASSWORD_KEY = "hass_password";
    public static final String PREF_BASIC_AUTH_KEY = "basic_auth";
    public static final String PREF_ALLOWED_HOST_MISMATCHES_KEY = "allowed_ssl_mismatches";
    public static final String PREF_ALLOWED_INVALID_SSL_CERTS_KEY = "allowed_invalid_ssl_certs";

    public static final String PREF_ENABLE_LOCATION_TRACKING = "enable_location_tracking";
    public static final String PREF_LOCATION_DEVICE_NAME = "location_device_name";
    public static final String PREF_LOCATION_UPDATE_INTERVAL = "location_update_interval";
    public static final String PREF_RESET_HOST_MISMATCHES = "reset_host_mismatches";
    public static final String HELP_TRANSLATE = "help_translate";

    public static final String NO_PASSWORD = "no-password-set";

    public static final int LIGHT_SUPPORTS_BRIGHTNESS = 1;
    public static final int LIGHT_SUPPORTS_COLOR_TEMP = 2;
    public static final int LIGHT_SUPPORTS_EFFECT = 4;
    public static final int LIGHT_SUPPORTS_FLASH = 8;
    public static final int LIGHT_SUPPORTS_RGB_COLOR = 16;
    public static final int LIGHT_SUPPORTS_TRANSITION = 32;
    public static final int LIGHT_SUPPORTS_XY_COLOR = 64;
    public static final int LIGHT_SUPPORTS_WHITE_VALUE = 128;

    public static final String CROWDIN_URL = "https://crowdin.com/project/home-assistant-android";

    private Common() {

    }
}