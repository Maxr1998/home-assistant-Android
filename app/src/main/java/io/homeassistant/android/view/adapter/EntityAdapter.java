package io.homeassistant.android.view.adapter;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.homeassistant.android.R;
import io.homeassistant.android.api.websocket.results.Entity;
import io.homeassistant.android.view.viewholders.BaseViewHolder;
import io.homeassistant.android.view.viewholders.CameraViewHolder;
import io.homeassistant.android.view.viewholders.ClimateViewHolder;
import io.homeassistant.android.view.viewholders.CoverViewHolder;
import io.homeassistant.android.view.viewholders.GroupViewHolder;
import io.homeassistant.android.view.viewholders.InputSelectViewHolder;
import io.homeassistant.android.view.viewholders.SceneViewHolder;
import io.homeassistant.android.view.viewholders.SensorViewHolder;
import io.homeassistant.android.view.viewholders.SwitchViewHolder;
import io.homeassistant.android.view.viewholders.TextViewHolder;

/**
 * Created by Nicolas on 2017-12-06.
 */

public class EntityAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private EntityList entities;
    private final BaseViewHolder.RequestSender sender;
    // todo track the recycled view pool for group view
    public final RecyclerView.RecycledViewPool recycledViewPool;
    private boolean isGroup=false;
    public EntityAdapter(BaseViewHolder.RequestSender sender, RecyclerView.RecycledViewPool recycledViewPool) {
        this.sender = sender;
        this.recycledViewPool = recycledViewPool;
    }

    public void setIsGroup(boolean b)
    {
        isGroup=b;
    }

    @Override
    public int getItemViewType(int position) {
        return entities.get(position).getType();
    }

    public void setEntities(EntityList entities)
    {
        this.entities = entities;
        notifyDataSetChanged();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, @ Entity.Type int viewType) {
        BaseViewHolder viewHolder=null;
        @LayoutRes int layout=-1;
        switch (viewType) {

            case Entity.TYPE_BASE:
                layout = R.layout.view_base;
                break;
            case Entity.TYPE_CAMERA:
                layout = R.layout.view_camera;
                break;
            case Entity.TYPE_CLIMATE:
                layout = R.layout.view_climate;
                break;
            case Entity.TYPE_COVER:
                layout = R.layout.view_cover;
                break;
            case Entity.TYPE_GROUP:
                layout = R.layout.view_group;
                break;
            case Entity.TYPE_INPUT_SELECT:
                layout = R.layout.view_input_select;
                break;
            case Entity.TYPE_SCENE:
                layout = R.layout.view_scene;
                break;
            case Entity.TYPE_SENSOR:
                layout = R.layout.view_sensor;
                break;
            case Entity.TYPE_SWITCH:
                layout = R.layout.view_switch;
                break;
            case Entity.TYPE_TEXT:
                layout = R.layout.view_text;
                break;
        }

        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        switch (viewType) {

            case Entity.TYPE_BASE:
                viewHolder = new BaseViewHolder(itemView,sender);
                break;
            case Entity.TYPE_CAMERA:
                viewHolder = new CameraViewHolder(itemView,sender);
                break;
            case Entity.TYPE_CLIMATE:
                viewHolder = new ClimateViewHolder(itemView,sender);
                break;
            case Entity.TYPE_COVER:
                viewHolder = new CoverViewHolder(itemView,sender);
                break;
            case Entity.TYPE_GROUP:
                viewHolder = new GroupViewHolder(itemView,this.recycledViewPool,sender);
                break;
            case Entity.TYPE_INPUT_SELECT:
                viewHolder = new InputSelectViewHolder(itemView,sender);
                break;
            case Entity.TYPE_SCENE:
                viewHolder = new SceneViewHolder(itemView,sender);
                break;
            case Entity.TYPE_SENSOR:
                viewHolder = new SensorViewHolder(itemView,sender);
                break;
            case Entity.TYPE_SWITCH:
                viewHolder = new SwitchViewHolder(itemView,sender);
                break;
            case Entity.TYPE_TEXT:
                viewHolder = new TextViewHolder(itemView,sender);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {

        holder.bind(entities.get(position), entities.getAllEntities(), isGroup);

    }

    @Override
    public int getItemCount() {
        return entities != null ? entities.size():0;
    }
}
