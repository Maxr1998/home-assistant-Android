package io.homeassistant.android.select;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.homeassistant.android.R;
import io.homeassistant.android.api.results.Entity;

public class SelectEntityViewAdapter extends RecyclerView.Adapter<SelectEntityViewAdapter.ItemViewHolder> {

    private final boolean allowMultiSelect;
    public List<Entity> entities = new ArrayList<>();
    private Set<Integer> selectedPositions = new HashSet<>();


    public SelectEntityViewAdapter(boolean multiSelect) {
        allowMultiSelect = multiSelect && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N; // MULTI-SELECT IS ONLY NEEDED / AVAILABLE ON N+!
    }

    @Override
    public SelectEntityViewAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectEntityViewAdapter.ItemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(allowMultiSelect ? R.layout.shortcut_item_multi_select : R.layout.shortcut_item, parent, false));
    }

    @SuppressLint("NewApi")
    @NonNull
    public Entity[] getSelected() {
        if (selectedPositions.isEmpty()) {
            return new Entity[]{null};
        }
        if (allowMultiSelect) {
            return selectedPositions.stream().map(integer -> entities.get(integer)).toArray(Entity[]::new);
        } else return new Entity[]{entities.get(selectedPositions.iterator().next())};
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.item.setChecked(selectedPositions.contains(holder.getAdapterPosition()));
        holder.item.setText(entities.get(holder.getAdapterPosition()).getFriendlyName());
        holder.item.setOnClickListener(v -> {
            if (!allowMultiSelect && selectedPositions.iterator().hasNext()) {
                int last = selectedPositions.iterator().next();
                selectedPositions.clear();
                notifyItemChanged(last);
            }
            int current = holder.getAdapterPosition();
            if (allowMultiSelect && selectedPositions.contains(current)) {
                selectedPositions.remove(current);
            } else selectedPositions.add(current);
            notifyItemChanged(current);
        });
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        final CompoundButton item;

        ItemViewHolder(View itemView) {
            super(itemView);
            item = (CompoundButton) itemView.findViewById(R.id.shortcut_item);
        }
    }
}