package io.homeassistant.android.viewholders;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import io.homeassistant.android.R;
import io.homeassistant.android.api.icons.MaterialDesignIconsUtils;
import io.homeassistant.android.api.results.Entity;


public class TextViewHolder extends BaseViewHolder {

    protected final TextView name;

    public TextViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.name);
        name.setOnTouchListener(null);
    }

    @Override
    public void setEntity(Entity e) {
        super.setEntity(e);
        name.setText(entity.attributes.friendly_name);
        try {
            Drawable icon = MaterialDesignIconsUtils.getInstance(name.getContext()).getDrawableFromName(name.getContext(), entity.attributes.icon);
            if (icon != null) {
                icon.setBounds(0, 0, name.getResources().getDimensionPixelSize(R.dimen.icon_size), name.getResources().getDimensionPixelSize(R.dimen.icon_size));
                if (this instanceof GroupViewHolder)
                    icon = null;
            }
            name.setCompoundDrawablesRelative(icon, null, null, null);
            name.setCompoundDrawablePadding(name.getResources().getDimensionPixelSize(R.dimen.icon_padding));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}