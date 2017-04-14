package io.homeassistant.android;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.afollestad.ason.Ason;

import io.homeassistant.android.api.results.RequestResult;
import io.homeassistant.android.view.LoginView;
import io.homeassistant.android.view.ViewAdapter;


public class HassActivity extends AppCompatActivity implements CommunicationHandler.ServiceCommunicator {

    private final Handler communicationHandler = new CommunicationHandler(this);
    @VisibleForTesting()
    public HassService service;
    private final ServiceConnection hassConnection = new ServiceConnection() {
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
            communicationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    customTabsSession.mayLaunchUrl(Uri.parse(Utils.getUrl(HassActivity.this)), null, null);
                }
            }, 1500);
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
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);

        getApplicationContext().bindService(new Intent(this, HassService.class), hassConnection, BIND_AUTO_CREATE);

        rootView = (FrameLayout) findViewById(R.id.root);
        mainLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator);

        if (Utils.getUrl(this).isEmpty() || Utils.getPassword(this).isEmpty()) {
            addLoginLayout();
        }

        swipeRefreshLayout = (SwipeRefreshLayout) mainLayout.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (service != null) {
                    service.loadStates();
                }
            }
        });
        swipeRefreshLayout.setRefreshing(true);

        RecyclerView viewRecycler = (RecyclerView) mainLayout.findViewById(R.id.view_recycler);
        viewRecycler.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(R.integer.view_columns), StaggeredGridLayoutManager.VERTICAL));
        viewRecycler.setAdapter(viewAdapter);
        viewRecycler.setRecycledViewPool(viewAdapter.recycledViewPool);
    }

    private void addLoginLayout() {
        mainLayout.setVisibility(View.GONE);
        loginLayout = (LoginView) getLayoutInflater().inflate(R.layout.custom_login, rootView, true).findViewById(R.id.login_layout);
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
        // Keep service alive for configuration changes. No connection will be leaked as it's bound to application context
        if (!isChangingConfigurations()) {
            getApplicationContext().unbindService(hassConnection);
        }
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

    public void attemptLogin() {
        // Force reconnect, this call is safe if not connected
        service.disconnect();
        service.connect();
    }

    @Override
    public void loginSuccess() {
        if (loginLayout != null) {
            rootView.removeView(loginLayout);
            loginLayout = null;
            mainLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void loginFailed(int reason) {
        if (loginLayout == null) {
            addLoginLayout();
        }
        loginLayout.showLoginError(reason);
    }

    @Override
    public void updateStates() {
        viewAdapter.updateEntities(service.getEntityMap());
        swipeRefreshLayout.setRefreshing(false);
    }

    public boolean send(Ason message, RequestResult.OnRequestResultListener resultListener) {
        return service.send(message, resultListener);
    }
}