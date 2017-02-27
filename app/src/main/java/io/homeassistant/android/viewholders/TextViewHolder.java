package io.homeassistant.android.viewholders;

import android.view.View;
import android.widget.TextView;

import io.homeassistant.android.R;
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
    }
}