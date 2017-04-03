package io.homeassistant.android.shortcuts;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.homeassistant.android.CommunicationHandler;
import io.homeassistant.android.HassService;
import io.homeassistant.android.R;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.requests.ToggleRequest;
import io.homeassistant.android.api.results.Entity;

import static io.homeassistant.android.HassService.EXTRA_ACTION_COMMAND;


public class ShortcutActivity extends AppCompatActivity implements CommunicationHandler.ServiceCommunicator {

    public static final String ACTION_SHORTCUT_LAUNCHED = "action_shortcut_launched";

    private static final String TAG = ShortcutActivity.class.getSimpleName();
    private final CommunicationHandler communicationHandler = new CommunicationHandler(this);
    private HassService service;
    private ServiceConnection hassConnection;

    private RecyclerView selectShortcutItemRecycler;
    private Spinner selectActionSpinner;

    private ViewAdapter viewAdapter = new ViewAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent serviceIntent = new Intent(this, HassService.class);

        switch (getIntent().getAction()) {
            case Intent.ACTION_CREATE_SHORTCUT:
                setContentView(R.layout.activity_create_shortcut);
                Toolbar t = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(t);
                hassConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder binder) {
                        service = ((HassService.HassBinder) binder).getService();
                        service.setActivityHandler(communicationHandler);
                        // Make sure that service is connected, if not it'll re-attempt
                        service.connect();
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        service.setActivityHandler(null);
                        service = null;
                    }
                };

                serviceIntent.setAction(TAG);
                bindService(serviceIntent, hassConnection, BIND_AUTO_CREATE);

                selectShortcutItemRecycler = (RecyclerView) findViewById(R.id.recycler_select_shortcut_item);
                selectShortcutItemRecycler.setLayoutManager(new LinearLayoutManager(this));
                selectShortcutItemRecycler.setItemAnimator(new DefaultItemAnimator());
                selectShortcutItemRecycler.setAdapter(viewAdapter);

                selectActionSpinner = (Spinner) findViewById(R.id.spinner_set_shortcut_type);
                ArrayAdapter adapter = new ArrayAdapter<>(this, android.support.design.R.layout.support_simple_spinner_dropdown_item, new String[]{"On", "Off"});
                selectActionSpinner.setAdapter(adapter);

                findViewById(R.id.button_add_shortcut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Entity selected = viewAdapter.entities.get(viewAdapter.selected);

                        Intent result = new Intent();
                        Intent shortcut = new Intent(ShortcutActivity.this, ShortcutActivity.class);
                        shortcut.setAction(ACTION_SHORTCUT_LAUNCHED);
                        boolean stateOn = selectActionSpinner.getSelectedItem().equals("On");
                        shortcut.putExtra(EXTRA_ACTION_COMMAND, new ToggleRequest(selected, stateOn).toString());
                        result.putExtra(Intent.EXTRA_SHORTCUT_NAME, HassUtils.extractEntityName(selected));
                        result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                                selected.id.startsWith("light") ? stateOn ? R.mipmap.ic_lightbulb_on : R.mipmap.ic_lightbulb_off : stateOn ? R.mipmap.ic_switch_on : R.mipmap.ic_switch_off));
                        result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut);

                        setResult(RESULT_OK, result);
                        finish();
                    }
                });
                break;
            case ACTION_SHORTCUT_LAUNCHED:
                String action = getIntent().getStringExtra(EXTRA_ACTION_COMMAND);
                Log.d(TAG, "Running shortcut with action " + action);
                serviceIntent.putExtra(EXTRA_ACTION_COMMAND, action);
                startService(serviceIntent);
                finish();
                break;
        }
    }

    @Override
    public void loginSuccess() {

    }

    @Override
    public void loginFailed(int reason) {
        finish();
        Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateStates() {
        for (Map.Entry<String, Entity> s : service.getEntityMap().entrySet()) {
            switch (HassUtils.extractDomainFromEntityId(s.getKey())) {
                case "automation":
                case "input_boolean":
                case "light":
                case "scene":
                case "switch":
                    Entity e = s.getValue();
                    if (e.attributes == null || !e.attributes.hidden)
                        viewAdapter.entities.add(e);
                    break;
            }
        }
        Collections.sort(viewAdapter.entities);
        viewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        if (hassConnection != null)
            unbindService(hassConnection);
        super.onDestroy();
    }

    private static class ViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        List<Entity> entities = new ArrayList<>();
        int selected = -1;

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.shortcut_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, int position) {
            Entity e = entities.get(holder.getAdapterPosition());
            holder.item.setChecked(holder.getAdapterPosition() == selected);
            holder.item.setText(HassUtils.extractEntityName(e));
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int last = selected;
                    selected = holder.getAdapterPosition();
                    notifyItemChanged(last);
                    notifyItemChanged(selected);
                }
            });
        }

        @Override
        public int getItemCount() {
            return entities.size();
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        RadioButton item;

        ItemViewHolder(View itemView) {
            super(itemView);
            item = (RadioButton) itemView.findViewById(R.id.shortcut_item);
        }
    }
}
