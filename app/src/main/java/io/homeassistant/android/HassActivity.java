package io.homeassistant.android;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.afollestad.ason.Ason;

import io.homeassistant.android.api.results.RequestResult;
import io.homeassistant.android.ui.LoginView;


public class HassActivity extends AppCompatActivity implements CommunicationHandler.ServiceCommunicator {

    private final Handler communicationHandler = new CommunicationHandler(this);
    private HassService service;
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
    private LoginView loginLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hass);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);

        bindService(new Intent(this, HassService.class), hassConnection, BIND_AUTO_CREATE);

        rootView = (FrameLayout) findViewById(R.id.root);
        mainLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator);

        if (Utils.getUrl(this).isEmpty() || Utils.getPassword(this).isEmpty()) {
            addLoginLayout();
        }

        RecyclerView viewRecycler = (RecyclerView) mainLayout.findViewById(R.id.view_recycler);
        viewRecycler.setLayoutManager(new LinearLayoutManager(this));
        viewRecycler.setItemAnimator(new DefaultItemAnimator());
        viewRecycler.setAdapter(viewAdapter);
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
        unbindService(hassConnection);
        unbindService(chromeConnection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String packageName = CustomTabsClient.getPackageName(this, null);
        CustomTabsClient.bindCustomTabsService(this, !TextUtils.isEmpty(packageName) ? packageName : "com.android.chrome", chromeConnection);
        getMenuInflater().inflate(R.menu.hass, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if (service != null) {
                    service.loadStates();
                }
                return true;
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
                    addLoginLayout();
                }
                return true;
            default:
                return false;
        }
    }

    public void attemptLogin() {
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
    }

    public boolean send(Ason message, RequestResult.OnRequestResultListener resultListener) {
        return service.send(message, resultListener);
    }
}