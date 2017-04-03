package io.homeassistant.android.viewholders;

import android.view.View;
import android.widget.TextView;

import io.homeassistant.android.R;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.results.Entity;

import static io.homeassistant.android.api.Domain.SUN;

public class SensorViewHolder extends TextViewHolder {

    private final TextView value;

    public SensorViewHolder(View itemView) {
        super(itemView);
        value = (TextView) itemView.findViewById(R.id.value);
    }

    @Override
    public void setEntity(Entity e) {
        super.setEntity(e);
        if (HassUtils.extractDomainFromEntityId(e.id).equals(SUN)) {
            entity.state = entity.state.replace('_', ' ');
        }

        String unit = entity.attributes.unit_of_measurement;
        value.setText(entity.state.concat(unit != null ? " " + unit : ""));
    }
}