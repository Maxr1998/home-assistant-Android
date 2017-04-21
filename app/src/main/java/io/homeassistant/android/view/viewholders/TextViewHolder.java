package io.homeassistant.android.view.viewholders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import io.homeassistant.android.R;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.icons.ImageUtils;
import io.homeassistant.android.api.results.Entity;

import static io.homeassistant.android.api.EntityType.CAMERA;
import static io.homeassistant.android.api.EntityType.GROUP;


public class TextViewHolder extends BaseViewHolder {

    protected final TextView name;

    @SuppressLint("ClickableViewAccessibility")
    public TextViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.name);
    }

    @Override
    public void setEntity(Entity e) {
        HassUtils.applyDefaultIcon(e);
        super.setEntity(e);
        name.setText(entity.attributes.friendly_name);
        name.setCompoundDrawablePadding(name.getResources().getDimensionPixelSize(R.dimen.icon_padding));
        name.setCompoundDrawablesRelative(null, null, null, null);
        if (entity.type != GROUP && entity.type != CAMERA) {
            try {
                ImageUtils.getInstance(name.getContext()).loadEntityDrawable(name.getContext(), entity, true, (drawable, async) -> {
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