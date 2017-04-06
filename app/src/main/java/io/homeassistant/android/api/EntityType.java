package io.homeassistant.android.api;

import android.support.annotation.LayoutRes;

import io.homeassistant.android.R;
import io.homeassistant.android.viewholders.BaseViewHolder;
import io.homeassistant.android.viewholders.GroupViewHolder;
import io.homeassistant.android.viewholders.InputSelectViewHolder;
import io.homeassistant.android.viewholders.SceneViewHolder;
import io.homeassistant.android.viewholders.SensorViewHolder;
import io.homeassistant.android.viewholders.SwitchViewHolder;
import io.homeassistant.android.viewholders.TextViewHolder;

public enum EntityType {
    BASE(R.layout.view_base, BaseViewHolder.class),
    TEXT(R.layout.view_text, TextViewHolder.class),
    GROUP(R.layout.view_group, GroupViewHolder.class),
    SWITCH(R.layout.view_switch, SwitchViewHolder.class),
    SENSOR(R.layout.view_sensor, SensorViewHolder.class),
    SCENE(R.layout.view_scene, SceneViewHolder.class),
    INPUT_SELECT(R.layout.view_input_select, InputSelectViewHolder.class);

    public final int layoutRes;
    public final Class<? extends BaseViewHolder> viewHolderClass;

    EntityType(@LayoutRes int layout, Class<? extends BaseViewHolder> c) {
        layoutRes = layout;
        viewHolderClass = c;
    }
}