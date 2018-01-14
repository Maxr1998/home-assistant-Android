package io.homeassistant.android.api;

import android.support.annotation.LayoutRes;

import io.homeassistant.android.R;
import io.homeassistant.android.view.viewholders.BaseViewHolder;
import io.homeassistant.android.view.viewholders.CameraViewHolder;
import io.homeassistant.android.view.viewholders.ClimateViewHolder;
import io.homeassistant.android.view.viewholders.CoverViewHolder;
import io.homeassistant.android.view.viewholders.GroupViewHolder;
import io.homeassistant.android.view.viewholders.InputSelectViewHolder;
import io.homeassistant.android.view.viewholders.MediaPlayerViewHolder;
import io.homeassistant.android.view.viewholders.SceneViewHolder;
import io.homeassistant.android.view.viewholders.SensorViewHolder;
import io.homeassistant.android.view.viewholders.SwitchViewHolder;
import io.homeassistant.android.view.viewholders.TextViewHolder;

public enum EntityType {
    BASE(R.layout.view_base, BaseViewHolder.class),
    CAMERA(R.layout.view_camera, CameraViewHolder.class),
    CLIMATE(R.layout.view_climate, ClimateViewHolder.class),
    COVER(R.layout.view_cover, CoverViewHolder.class),
    GROUP(R.layout.view_group, GroupViewHolder.class),
    INPUT_SELECT(R.layout.view_input_select, InputSelectViewHolder.class),
    MEDIA_PLAYER(R.layout.view_media_player, MediaPlayerViewHolder.class),
    SENSOR(R.layout.view_sensor, SensorViewHolder.class),
    SCENE(R.layout.view_scene, SceneViewHolder.class),
    SWITCH(R.layout.view_switch, SwitchViewHolder.class),
    TEXT(R.layout.view_text, TextViewHolder.class);


    public final int layoutRes;
    public final Class<? extends BaseViewHolder> viewHolderClass;

    EntityType(@LayoutRes int layout, Class<? extends BaseViewHolder> c) {
        layoutRes = layout;
        viewHolderClass = c;
    }
}