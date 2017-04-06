package io.homeassistant.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.homeassistant.android.api.EntityType;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.results.Entity;
import io.homeassistant.android.viewholders.BaseViewHolder;
import io.homeassistant.android.viewholders.GroupViewHolder;


public class ViewAdapter extends RecyclerView.Adapter<GroupViewHolder> {

    final RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
    private final Context context;
    private final List<Pair<Entity, List<Entity>>> entities;

    public ViewAdapter(Context c) {
        context = c;
        entities = new ArrayList<>();
    }

    private static <T extends BaseViewHolder> T createViewHolder(EntityType type, LayoutInflater inflater) {
        BaseViewHolder viewHolder;
        View itemView = inflater.inflate(type.layoutRes, null);
        try {
            viewHolder = type.viewHolderClass.getConstructor(View.class).newInstance(itemView);
        } catch (ReflectiveOperationException e) {
            viewHolder = null;
        }
        return (T) viewHolder;
    }

    void updateEntities(Map<String, Entity> entityMap) {
        HassUtils.extractGroups(entityMap, entities);
        notifyDataSetChanged();
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Only group items
        GroupViewHolder holder = createViewHolder(EntityType.GROUP, LayoutInflater.from(context));
        holder.childRecycler.setRecycledViewPool(recycledViewPool);
        return holder;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        holder.setEntity(entities.get(position).first);
        holder.adapter.updateChildren(entities.get(position).second);
        holder.space.setVisibility(position >= getItemCount() - holder.itemView.getResources().getInteger(R.integer.view_columns) ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemViewType(int position) {
        return entities.get(position).first.type.ordinal();
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    public static class ChildViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {

        private List<Entity> children = Collections.emptyList();

        void updateChildren(List<Entity> c) {
            children = c;
            notifyDataSetChanged();
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return ViewAdapter.createViewHolder(EntityType.values()[viewType], LayoutInflater.from(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            holder.setEntity(children.get(position));
        }

        @Override
        public int getItemViewType(int position) {
            return children.get(position).type.ordinal();
        }

        @Override
        public int getItemCount() {
            return children.size();
        }
    }
}