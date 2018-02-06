package io.homeassistant.android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.homeassistant.android.view.LoginView;
import io.homeassistant.android.view.ViewAdapter;
import io.homeassistant.android.wearable.WearableCredentialsSync;

import static io.homeassistant.android.CommunicationHandler.FAILURE_REASON_BASIC_AUTH;
import static io.homeassistant.android.CommunicationHandler.FAILURE_REASON_GENERIC;


public class HassActivity extends BaseActivity {

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
            communicationHandler.postDelayed(() -> customTabsSession.mayLaunchUrl(Uri.parse(Utils.getUrl(HassActivity.this)), null, null), 1500);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private ViewAdapter viewAdapter = new ViewAdapter(this);
    private FrameLayout rootView;
    private CoordinatorLayout mainLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LoginView loginLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hass);
        Toolbar t = findViewById(R.id.toolbar);
        setSupportActionBar(t);

        rootView = findViewById(R.id.root);
        mainLayout = findViewById(R.id.main_coordinator);

        if (Utils.getUrl(this).isEmpty() || Utils.getPassword(this).isEmpty()) {
            addLoginLayout();
        }

        swipeRefreshLayout = mainLayout.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (service != null) {
                service.loadStates();
            }
        });
        swipeRefreshLayout.setRefreshing(true);

        RecyclerView viewRecycler = mainLayout.findViewById(R.id.view_recycler);
        viewRecycler.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(R.integer.view_columns), StaggeredGridLayoutManager.VERTICAL));
        viewRecycler.setAdapter(viewAdapter);
        viewRecycler.setRecycledViewPool(viewAdapter.recycledViewPool);
    }

    private void addLoginLayout() {
        mainLayout.setVisibility(View.GONE);
        loginLayout = getLayoutInflater().inflate(R.layout.custom_login, rootView, true).findViewById(R.id.login_layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (service != null) {
            // Make sure that service is connected, if not it'll re-attempt
            service.connect();
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(chromeConnection);
        super.onDestroy();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        String packageName = CustomTabsClient.getPackageName(this, null);
        CustomTabsClient.bindCustomTabsService(this, !TextUtils.isEmpty(packageName) ? packageName : "com.android.chrome", chromeConnection);
        getMenuInflater().inflate(R.menu.hass, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_custom_tab:
                if (customTabsSession != null) {
                    @SuppressWarnings("deprecation") CustomTabsIntent intent = new CustomTabsIntent.Builder(customTabsSession)
                            .setShowTitle(true)
                            .enableUrlBarHiding()
                            .setToolbarColor(getResources().getColor(R.color.primary))
                            .build();
                    intent.launchUrl(this, Uri.parse(Utils.getUrl(this)));
                }
                return true;
            case R.id.menu_logout:
                if (loginLayout == null) {
                    Utils.getPrefs(this).edit().remove(Common.PREF_HASS_PASSWORD_KEY).apply();
                    service.disconnect();
                    addLoginLayout();
                }
                return true;
            case R.id.menu_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void loginSuccess() {
        if (loginLayout != null) {
            rootView.removeView(loginLayout);
            loginLayout = null;
            mainLayout.setVisibility(View.VISIBLE);
        }
        WearableCredentialsSync.transferCredentials(this);
    }

    @Override
    public void loginFailed(int reason, String data) {
        if (reason == FAILURE_REASON_BASIC_AUTH) {
            LayoutInflater inflater = LayoutInflater.from(this);
            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.dialog_basic_auth, null);
            TextView dialogText = dialogView.findViewById(R.id.dialog_basic_auth_text);
            dialogText.setText(getString(R.string.dialog_basic_auth_text, Utils.getUrl(this)));

            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_basic_auth_title)
                    .setView(dialogView)
                    .setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> loginFailed(FAILURE_REASON_GENERIC, null)))
                    .setPositiveButton(R.string.dialog_basic_auth_button_login, (dialogInterface, i) -> {
                        TextInputEditText dialogUsername = dialogView.findViewById(R.id.dialog_basic_auth_username);
                        TextInputEditText dialogPassword = dialogView.findViewById(R.id.dialog_basic_auth_password);
                        Utils.setBasicAuth(this, dialogUsername.getText().toString(), dialogPassword.getText().toString());
                        attemptLogin();
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        if (loginLayout == null) {
            addLoginLayout();
        }
        loginLayout.showLoginError(reason, data);
    }

    @Override
    public void updateStates() {
        viewAdapter.updateEntities(service.getEntityMap());
        swipeRefreshLayout.setRefreshing(false);
    }
}