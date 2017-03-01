package io.homeassistant.android.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
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


public class LoginView extends LinearLayout {

    private TextInputLayout urlInputLayout;
    private TextInputEditText urlInput;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordInput;
    private Button connectButton;
    private ProgressBar progress;

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

        connectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.getPrefs(getContext()).edit()
                        .putString(Common.PREF_HASS_URL_KEY, urlInput.getText().toString())
                        .putString(Common.PREF_HASS_PASSWORD_KEY, passwordInput.getText().toString()).apply();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
                connectButton.setVisibility(INVISIBLE);
                progress.setVisibility(VISIBLE);
                ((HassActivity) getContext()).attemptLogin();
            }
        });
    }

    private void updateConnectButton() {
        connectButton.setEnabled(!urlInput.getText().toString().isEmpty()
                && !passwordInput.getText().toString().isEmpty()
                && TextUtils.isEmpty(passwordInputLayout.getError()));
    }

    public void showLoginError() {
        progress.setVisibility(INVISIBLE);
        connectButton.setVisibility(VISIBLE);
        Toast.makeText(getContext(), R.string.login_error, Toast.LENGTH_SHORT).show();
    }
}