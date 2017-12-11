package io.homeassistant.android.view.viewholders;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import io.homeassistant.android.R;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.ImageUtils;
import io.homeassistant.android.api.websocket.results.Entity;


public class TextViewHolder extends BaseViewHolder {

    protected final TextView name;

    @SuppressLint("ClickableViewAccessibility")
    public TextViewHolder(View itemView, RequestSender sender) {
        super(itemView, sender);
        name = (TextView) itemView.findViewById(R.id.name);
    }

    @Override
    protected void updateViews() {
        HassUtils.applyDefaultIcon(entity);
        name.setText(entity.getFriendlyName());
        name.setCompoundDrawablePadding(name.getResources().getDimensionPixelSize(R.dimen.icon_padding));
        name.setCompoundDrawablesRelative(null, null, null, null);
        if (entity.getType() != Entity.TYPE_GROUP && entity.getType() != Entity.TYPE_CAMERA) {
            try {
                ImageUtils.getInstance(name.getContext()).loadEntityDrawable(name.getContext(), entity, true, (drawable, async) -> {
                    if (drawable != null)
                        drawable.setBounds(0, 0, name.getResources().getDimensionPixelSize(R.dimen.icon_size), name.getResources().getDimensionPixelSize(R.dimen.icon_size));

                    if (async)
                        name.post(() -> name.setCompoundDrawablesRelative(drawable, null, null, null));
                    else name.setCompoundDrawablesRelative(drawable, null, null, null);
                });
            } catch (Exception ex) {
                Log.e("TextViewHolder","Error",ex);
            }
        }
    }
}