package io.homeassistant.android;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import java.util.Arrays;

import io.homeassistant.android.location.LocationUpdateReceiver;


public class SettingsActivity extends AppCompatActivity {

    private CustomTabsSession customTabsSession;
    private final CustomTabsServiceConnection chromeConnection = new CustomTabsServiceConnection() {
        @Override
        public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
            client.warmup(0);
            customTabsSession = client.newSession(new CustomTabsCallback());
            if (customTabsSession == null) {
                return;
            }
            // Delay to not slow down native app loading
            customTabsSession.mayLaunchUrl(Uri.parse(Common.CROWDIN_URL), null, null);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String packageName = CustomTabsClient.getPackageName(this, null);
        CustomTabsClient.bindCustomTabsService(this, !TextUtils.isEmpty(packageName) ? packageName : "com.android.chrome", chromeConnection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Settings settings = (Settings) getFragmentManager().findFragmentById(R.id.settings_fragment);
        settings.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent up = NavUtils.getParentActivityIntent(this);
                up.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, up);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        unbindService(chromeConnection);
        super.onDestroy();
    }

    public CustomTabsSession getCustomTabsSession() {
        return customTabsSession;
    }

    public static class Settings extends PreferenceFragment {

        private SharedPreferences prefs;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            prefs = Utils.getPrefs(getActivity());
            addPreferencesFromResource(R.xml.preferences);
            updatePreferenceSummaries();
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            View preferenceView = getListView().findViewHolderForAdapterPosition(preference.getOrder()).itemView;

            switch (preference.getKey()) {
                case Common.PREF_LOCATION_UPDATE_INTERVAL:
                    ListPopupWindow listPopupWindow = new ListPopupWindow(getActivity());
                    listPopupWindow.setAnchorView(preferenceView);
                    listPopupWindow.setAdapter(new ArrayAdapter<>(getActivity(), android.support.design.R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.location_update_interval_summaries)));
                    listPopupWindow.setContentWidth(getResources().getDimensionPixelSize(R.dimen.popup_window_width));
                    listPopupWindow.setHorizontalOffset(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
                    listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
                        Log.d("Selected", String.valueOf(position));
                        prefs.edit().putInt(Common.PREF_LOCATION_UPDATE_INTERVAL,
                                getResources().getIntArray(R.array.location_update_interval_values)[position]).apply();
                        listPopupWindow.dismiss();
                        updatePreferenceSummaries();
                        updateLocationTracker();
                    });
                    listPopupWindow.show();
                    return true;
                case Common.PREF_RESET_HOST_MISMATCHES:
                    prefs.edit().remove(Common.PREF_ALLOWED_HOST_MISMATCHES_KEY).apply();
                    Toast.makeText(getActivity(), R.string.toast_ignored_ssl_mismatches_cleared, Toast.LENGTH_SHORT).show();
                    updatePreferenceSummaries();
                    return true;
                case Common.HELP_TRANSLATE:
                    CustomTabsSession session = ((SettingsActivity) getActivity()).getCustomTabsSession();
                    if (session != null) {
                        @SuppressWarnings("deprecation") CustomTabsIntent intent = new CustomTabsIntent.Builder(session)
                                .setShowTitle(true)
                                .enableUrlBarHiding()
                                .setToolbarColor(getResources().getColor(R.color.primary))
                                .build();
                        intent.launchUrl(getActivity(), Uri.parse(Common.CROWDIN_URL));
                    }
                    return true;
                default:
                    return super.onPreferenceTreeClick(preference);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (permissions.length > 0 && grantResults.length > 0) {
                if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((SwitchPreference) findPreference(Common.PREF_ENABLE_LOCATION_TRACKING)).setChecked(true);
                }
            }
        }

        private void updatePreferenceSummaries() {
            Preference enableLocationTracking = findPreference(Common.PREF_ENABLE_LOCATION_TRACKING);
            enableLocationTracking.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue == Boolean.TRUE && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                    return false;
                }
                updateLocationTracker();
                return true;
            });

            Preference deviceName = findPreference(Common.PREF_LOCATION_DEVICE_NAME);
            deviceName.setSummary(getResources().getString(R.string.preference_location_device_name_summary, prefs.getString(deviceName.getKey(), "nothing")));
            deviceName.setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary(getResources().getString(R.string.preference_location_device_name_summary, newValue));
                updateLocationTracker();
                return true;
            });

            Preference updateInterval = findPreference(Common.PREF_LOCATION_UPDATE_INTERVAL);
            int selectedIndex = Arrays.binarySearch(getResources().getIntArray(R.array.location_update_interval_values), prefs.getInt(updateInterval.getKey(), 10));
            updateInterval.setSummary(getResources().getStringArray(R.array.location_update_interval_summaries)[selectedIndex]);

            Preference resetIgnoredSSLMismatches = findPreference(Common.PREF_RESET_HOST_MISMATCHES);
            if (prefs.getStringSet(Common.PREF_ALLOWED_HOST_MISMATCHES_KEY, null) == null) {
                resetIgnoredSSLMismatches.setSummary(R.string.preference_reset_host_mismatches_summary_empty);
                resetIgnoredSSLMismatches.setEnabled(false);
            }
        }

        private void updateLocationTracker() {
            Intent intent = new Intent(getActivity(), LocationUpdateReceiver.class);
            intent.setAction(LocationUpdateReceiver.ACTION_START_LOCATION);
            getActivity().sendBroadcast(intent);
        }
    }
}