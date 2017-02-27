package io.homeassistant.android.api.requests;

import com.afollestad.ason.Ason;
import com.afollestad.ason.AsonName;

public class AuthRequest extends Ason {
    protected final String type = "auth";
    @AsonName(name = "api_password")
    protected final String password;

    public AuthRequest(String pass) {
        password = pass;
    }
}