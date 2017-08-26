package io.homeassistant.android.api.requests;

import com.afollestad.ason.AsonName;

public class AuthRequest extends HassRequest {
    @AsonName(name = "api_password")
    protected final String password;

    public AuthRequest(String pass) {
        super("auth");
        password = pass;
    }
}