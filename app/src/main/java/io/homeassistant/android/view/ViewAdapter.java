package io.homeassistant.android.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.homeassistant.android.R;
import io.homeassistant.android.api.HassUtils;
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


public class ViewAdapter extends RecyclerView.Adapter<GroupViewHolder> {

    public final RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
    private final Context context;
    private final List<Pair<Entity, List<Entity>>> entities;
    private final BaseViewHolder.RequestSender sender;

    public ViewAdapter(Context c, BaseViewHolder.RequestSender sender) {
        context = c;
        this.sender = sender;
        entities = new ArrayList<>();
    }

    private static BaseViewHolder createViewHolder(@Entity.Type int type,
                                                                 LayoutInflater inflater,
                                                                 ViewGroup parent,
                                                                 BaseViewHolder.RequestSender sender
    ) {




        BaseViewHolder viewHolder=null;
        @LayoutRes int layout=-1;
        switch (type) {

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



        View itemView = inflater.inflate(layout, parent, false);

        switch (type) {

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
                viewHolder = new GroupViewHolder(itemView,sender);
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

    public void updateEntities(Map<String, Entity> entityMap) {
        HassUtils.extractGroups(entityMap, entities, false);
        notifyDataSetChanged();
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Only group items

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group, parent, false);

        GroupViewHolder holder = new GroupViewHolder(itemView,sender);
        holder.childRecycler.setRecycledViewPool(recycledViewPool);
        return holder;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        holder.bind(entities.get(position).first);
        holder.adapter.updateChildren(entities.get(position).second);
        holder.space.setVisibility(position >= getItemCount() - holder.itemView.getResources().getInteger(R.integer.view_columns) ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemViewType(int position) {
        return Entity.TYPE_GROUP;
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    public static class ChildViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {

        private List<Entity> children = Collections.emptyList();
        private final BaseViewHolder.RequestSender sender;

        public ChildViewAdapter(BaseViewHolder.RequestSender sender) {
            this.sender = sender;
        }

        void updateChildren(List<Entity> c) {
            children = c;
            notifyDataSetChanged();
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return ViewAdapter.createViewHolder(viewType, LayoutInflater.from(parent.getContext()), parent,sender);
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            holder.bind(children.get(position));
        }

        @Override
        public int getItemViewType(int position) {
            return children.get(position).type;
        }

        @Override
        public int getItemCount() {
            return children.size();
        }
    }
}