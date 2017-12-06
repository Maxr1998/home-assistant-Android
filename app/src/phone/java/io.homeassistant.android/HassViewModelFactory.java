package io.homeassistant.android;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import io.homeassistant.android.api.websocket.HassWebSockerApi;

/**
 * Created by Nicolas on 2017-11-26.
 */

public class HassViewModelFactory implements ViewModelProvider.Factory {

    final private HassWebSockerApi hassApi;

    public HassViewModelFactory(HassWebSockerApi hassApi) {
        this.hassApi = hassApi;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HassViewModel.class)) {
            return (T) new HassViewModel(hassApi);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
