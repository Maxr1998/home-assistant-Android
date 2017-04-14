package io.homeassistant.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public static class Settings extends PreferenceFragment {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            return super.onPreferenceTreeClick(preference);
        }
    }
}