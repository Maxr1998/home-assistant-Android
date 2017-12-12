package io.homeassistant.android;

import android.content.Context;

import io.homeassistant.android.select.SelectEntityActivity;
import io.homeassistant.android.select.SelectViewModelFactory;

/**
 * Created by Nicolas on 2017-12-06.
 */

public class PhoneInjector extends ApiInjector{

    public static HassViewModelFactory getHassViewModelFactory(Context context)
    {
        return new HassViewModelFactory(getHassApi(context));
    }

    public static SelectViewModelFactory getSelectiewModelFactory(Context context) {
        return new SelectViewModelFactory(context);
    }
}
