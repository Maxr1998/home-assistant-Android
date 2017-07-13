package io.homeassistant.android.shortcuts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.homeassistant.android.HassService;

import static io.homeassistant.android.HassService.EXTRA_ACTION_COMMAND;


public class ShortcutActivity extends AppCompatActivity {

    public static final String ACTION_SHORTCUT_LAUNCHED = "action_shortcut_launched";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent serviceIntent = new Intent(this, HassService.class);
        if (getIntent().getAction().equals(ACTION_SHORTCUT_LAUNCHED)) {
            String action = getIntent().getStringExtra(EXTRA_ACTION_COMMAND);
            serviceIntent.putExtra(EXTRA_ACTION_COMMAND, action);
            startService(serviceIntent);
            finish();
        }
    }
}