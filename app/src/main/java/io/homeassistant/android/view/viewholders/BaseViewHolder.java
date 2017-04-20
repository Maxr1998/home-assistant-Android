package io.homeassistant.android.view.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import io.homeassistant.android.api.results.Entity;


public class BaseViewHolder extends RecyclerView.ViewHolder {

    protected Entity entity;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public void setEntity(Entity e) {
        entity = e;
    }
}