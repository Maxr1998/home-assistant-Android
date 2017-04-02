package io.homeassistant.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.homeassistant.android.api.EntityType;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.results.Entity;
import io.homeassistant.android.viewholders.BaseViewHolder;


public class ViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private final Context context;
    private final List<Entity> entities;

    public ViewAdapter(Context c) {
        context = c;
        entities = new ArrayList<>();
    }

    private <T extends BaseViewHolder> T createViewHolder(EntityType type, LayoutInflater inflater) {
        BaseViewHolder viewHolder;
        View itemView = inflater.inflate(type.layoutRes, null);
        try {
            viewHolder = type.viewHolderClass.getConstructor(View.class).newInstance(itemView);
        } catch (ReflectiveOperationException e) {
            viewHolder = null;
        }
        return (T) viewHolder;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return createViewHolder(EntityType.values()[viewType], LayoutInflater.from(context));
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        Entity entity = entities.get(position);
        holder.setEntity(entity);
    }

    public void updateEntities(Map<String, Entity> entityMap) {
        HassUtils.extractGroups(entityMap, entities);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return entities.get(position).type.ordinal();
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }
}