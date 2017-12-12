package io.homeassistant.android.select;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import io.homeassistant.android.HassViewModel;
import io.homeassistant.android.api.websocket.HassWebSockerApi;

/**
 * Created by Nicolas on 2017-12-11.
 */

public class SelectViewModelFactory implements ViewModelProvider.Factory{

    final private Context context;

    public SelectViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SelectViewModel.class)) {
            return (T) new SelectViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
