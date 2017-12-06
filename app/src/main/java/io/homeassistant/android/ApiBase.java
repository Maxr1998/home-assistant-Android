package io.homeassistant.android;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Map;

import io.homeassistant.android.api.websocket.results.Entity;

public interface ApiBase {

    void attemptLogin();
}
