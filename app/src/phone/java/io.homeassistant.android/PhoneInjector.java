package io.homeassistant.android;

import android.content.Context;

/**
 * Created by Nicolas on 2017-12-06.
 */

public class PhoneInjector extends ApiInjector{

    public static HassViewModelFactory getHassViewModelFactory(Context context)
    {
        return new HassViewModelFactory(getHassApi(context));
    }

}
