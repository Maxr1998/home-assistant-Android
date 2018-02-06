package io.homeassistant.android.select;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Collections;
import java.util.Map;

import io.homeassistant.android.BaseActivity;
import io.homeassistant.android.R;
import io.homeassistant.android.api.requests.ToggleRequest;
import io.homeassistant.android.api.results.Entity;
import io.homeassistant.android.shortcuts.ShortcutActivity;

import static io.homeassistant.android.HassService.EXTRA_ACTION_COMMAND;
import static io.homeassistant.android.api.Domain.AUTOMATION;
import static io.homeassistant.android.api.Domain.INPUT_BOOLEAN;
import static io.homeassistant.android.api.Domain.LIGHT;
import static io.homeassistant.android.api.Domain.SCENE;
import static io.homeassistant.android.api.Domain.SWITCH;


public class SelectEntityActivity extends BaseActivity {

    public static final String ACTION_SHORTCUT_LAUNCHED = "action_shortcut_launched";

    private RecyclerView selectShortcutItemRecycler;
    private Spinner selectActionSpinner;

    private SelectEntityViewAdapter viewAdapter = new SelectEntityViewAdapter(false);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getAction().equals(Intent.ACTION_CREATE_SHORTCUT)) {
            setContentView(R.layout.activity_create_shortcut);
            Toolbar t = findViewById(R.id.toolbar);
            setSupportActionBar(t);

            selectShortcutItemRecycler = findViewById(R.id.recycler_select_shortcut_item);
            selectShortcutItemRecycler.setLayoutManager(new LinearLayoutManager(this));
            selectShortcutItemRecycler.setItemAnimator(new DefaultItemAnimator());
            selectShortcutItemRecycler.setAdapter(viewAdapter);

            selectActionSpinner = findViewById(R.id.spinner_set_shortcut_type);
            ArrayAdapter adapter = new ArrayAdapter<>(this, android.support.design.R.layout.support_simple_spinner_dropdown_item, new String[]{"On", "Off"});
            selectActionSpinner.setAdapter(adapter);

            findViewById(R.id.button_add_shortcut).setOnClickListener(v -> {
                Entity selected = viewAdapter.getSelected()[0];
                if (selected != null) {
                    Intent result = new Intent();
                    Intent shortcut = new Intent(SelectEntityActivity.this, ShortcutActivity.class);
                    shortcut.setAction(ACTION_SHORTCUT_LAUNCHED);
                    boolean stateOn = selectActionSpinner.getSelectedItem().equals("On");
                    shortcut.putExtra(EXTRA_ACTION_COMMAND, new ToggleRequest(selected, stateOn).toString());
                    result.putExtra(Intent.EXTRA_SHORTCUT_NAME, selected.getFriendlyName());
                    result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                            selected.id.startsWith("light") ? stateOn ? R.mipmap.ic_lightbulb_on : R.mipmap.ic_lightbulb_off : stateOn ? R.mipmap.ic_switch_on : R.mipmap.ic_switch_off));
                    result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut);

                    setResult(RESULT_OK, result);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
            });
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void loginSuccess() {
    }

    @Override
    public void loginFailed(int reason, String data) {
        finish();
        Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateStates() {
        for (Map.Entry<String, Entity> s : service.getEntityMap().entrySet()) {
            Entity e = s.getValue();
            switch (e.getDomain()) {
                case AUTOMATION:
                case INPUT_BOOLEAN:
                case LIGHT:
                case SCENE:
                case SWITCH:
                    if (!e.isHidden())
                        viewAdapter.entities.add(e);
                    break;
            }
        }
        Collections.sort(viewAdapter.entities);
        viewAdapter.notifyDataSetChanged();
    }
}