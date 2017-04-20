package io.homeassistant.android.view.viewholders;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import io.homeassistant.android.R;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.icons.ImageUtils;
import io.homeassistant.android.api.results.Entity;

import static io.homeassistant.android.api.EntityType.GROUP;


public class TextViewHolder extends BaseViewHolder {

    protected final TextView name;

    @SuppressLint("ClickableViewAccessibility")
    public TextViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.name);
        name.setOnTouchListener(null);
    }

    @Override
    public void setEntity(Entity e) {
        HassUtils.applyDefaultIcon(e);
        super.setEntity(e);
        name.setText(entity.attributes.friendly_name);
        try {
            Drawable icon = entity.type != GROUP ? ImageUtils.getInstance(name.getContext()).getEntityDrawable(entity) : null;
            if (icon != null) {
                icon.setBounds(0, 0, name.getResources().getDimensionPixelSize(R.dimen.icon_size), name.getResources().getDimensionPixelSize(R.dimen.icon_size));
            }
            name.setCompoundDrawablesRelative(icon, null, null, null);
            name.setCompoundDrawablePadding(name.getResources().getDimensionPixelSize(R.dimen.icon_padding));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}