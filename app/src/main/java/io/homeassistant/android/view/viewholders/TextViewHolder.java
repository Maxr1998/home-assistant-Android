package io.homeassistant.android.view.viewholders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import io.homeassistant.android.R;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.icons.ImageUtils;

import static io.homeassistant.android.api.EntityType.CAMERA;
import static io.homeassistant.android.api.EntityType.GROUP;
import static io.homeassistant.android.api.EntityType.MEDIA_PLAYER;


public class TextViewHolder extends BaseViewHolder {

    protected final TextView name;

    @SuppressLint("ClickableViewAccessibility")
    public TextViewHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
    }

    @Override
    protected void updateViews() {
        if (entity.type == MEDIA_PLAYER)
            return;
        name.setText(entity.getFriendlyName());
        if (entity.type != GROUP && entity.type != CAMERA) {
            HassUtils.applyDefaultIcon(entity);
            try {
                ImageUtils.getInstance(name.getContext()).getEntityDrawable(name.getContext(), entity, (drawable, async) -> {
                    if (drawable != null)
                        drawable.setBounds(0, 0, name.getResources().getDimensionPixelSize(R.dimen.icon_size), name.getResources().getDimensionPixelSize(R.dimen.icon_size));

                    if (async)
                        name.post(() -> name.setCompoundDrawablesRelative(drawable, null, null, null));
                    else name.setCompoundDrawablesRelative(drawable, null, null, null);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}