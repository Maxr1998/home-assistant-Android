package io.homeassistant.android.view;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.homeassistant.android.Common;
import io.homeassistant.android.HassActivity;
import io.homeassistant.android.R;
import io.homeassistant.android.Utils;
import okhttp3.HttpUrl;

import static io.homeassistant.android.CommunicationHandler.FAILURE_REASON_GENERIC;
import static io.homeassistant.android.CommunicationHandler.FAILURE_REASON_SSL_MISMATCH;
import static io.homeassistant.android.CommunicationHandler.FAILURE_REASON_WRONG_PASSWORD;


public class LoginView extends LinearLayout {

    private TextInputLayout urlInputLayout;
    private TextInputEditText urlInput;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordInput;
    private Button connectButton;
    private ProgressBar progress;

    private ConnectivityManager connectivityManager;
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateConnectButton();
        }
    };

    public LoginView(Context context) {
        super(context);
    }

    public LoginView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoginView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        urlInputLayout = (TextInputLayout) findViewById(R.id.url_input_layout);
        urlInput = (TextInputEditText) findViewById(R.id.url_input);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.password_input_layout);
        passwordInput = (TextInputEditText) findViewById(R.id.password_input);
        connectButton = (Button) findViewById(R.id.connect_button);
        progress = (ProgressBar) findViewById(android.R.id.progress);

        urlInputLayout.setErrorEnabled(true);
        if (!Utils.getUrl(getContext()).isEmpty()) {
            urlInput.setText(Utils.getUrl(getContext()));
        }
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentInput = s.toString();
                if (!currentInput.matches("http(s)?://[\\s\\S]*")) {
                    currentInput = "http://".concat(currentInput);
                }
                urlInputLayout.setError(HttpUrl.parse(currentInput) == null && !s.toString().isEmpty() ? getResources().getString(R.string.invalid_url_scheme) : null);
                updateConnectButton();
            }
        });

        passwordInputLayout.setPasswordVisibilityToggleEnabled(true);
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateConnectButton();
            }
        });

        // Hint that existing password is used when there is one available (and it isn't a fake one for open instances)
        String savedPassword = Utils.getPassword(getContext());
        if (!savedPassword.isEmpty() && !savedPassword.equals(Common.NO_PASSWORD))
            passwordInputLayout.setHint(getResources().getString(R.string.hint_password_existing));

        connectButton.setOnClickListener(v -> {
            SharedPreferences.Editor prefs = Utils.getPrefs(getContext()).edit();
            // Tidying up the URL format
            String url = urlInput.getText().toString();
            if (!url.matches("http(s)?://[\\s\\S]*")) {
                url = "http://".concat(url);
            }
            if (url.charAt(url.length() - 1) == '/') {
                url = url.substring(0, url.length() - 1);
            }
            prefs.putString(Common.PREF_HASS_URL_KEY, url);
            String password = passwordInput.getText().toString();
            if (!password.isEmpty()) {
                prefs.putString(Common.PREF_HASS_PASSWORD_KEY, password);
            } else if (Utils.getPassword(getContext()).isEmpty()) {
                // Set fake password to support open instances if none was provided
                prefs.putString(Common.PREF_HASS_PASSWORD_KEY, Common.NO_PASSWORD);
            }
            prefs.apply();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
            connectButton.setVisibility(INVISIBLE);
            progress.setVisibility(VISIBLE);
            ((HassActivity) getContext()).attemptLogin();
        });
        connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        updateConnectButton();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(networkReceiver);
    }

    private boolean isConnected() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * Update the state of the connect button: require a valid URL and network access to allow clicking
     */
    private void updateConnectButton() {
        connectButton.setEnabled(isConnected() &&
                !urlInput.getText().toString().isEmpty()
                && TextUtils.isEmpty(urlInputLayout.getError()));

        connectButton.setText(isConnected() ? R.string.button_connect : R.string.button_connect_no_network);
    }

    public void showLoginError(int reason) {
        progress.setVisibility(INVISIBLE);
        connectButton.setVisibility(VISIBLE);

        @StringRes int message;
        switch (reason) {
            default:
            case FAILURE_REASON_GENERIC:
                message = R.string.login_error_generic;
                break;
            case FAILURE_REASON_WRONG_PASSWORD:
                message = R.string.login_error_wrong_password;
                break;
            case FAILURE_REASON_SSL_MISMATCH:
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.dialog_login_error_ssl_mismatch_title)
                        .setMessage(R.string.dialog_login_error_ssl_mismatch_message)
                        .setPositiveButton(android.R.string.cancel, null)
                        .setNeutralButton(R.string.dialog_login_error_ssl_mismatch_ignore, (dialog, which) -> {
                            Utils.addAllowedHostMismatch(getContext(), HttpUrl.parse(Utils.getUrl(getContext())).host());
                            connectButton.callOnClick();
                        })
                        .setCancelable(false)
                        .create().show();
                return;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}