package io.homeassistant.android.view.viewholders;

import android.view.View;
import android.widget.TextView;

import io.homeassistant.android.R;
import io.homeassistant.android.api.Attribute;

public class SensorViewHolder extends TextViewHolder {

    private final TextView value;

    public SensorViewHolder(View itemView) {
        super(itemView);
        value = (TextView) itemView.findViewById(R.id.value);
    }

    @Override
    protected void updateViews() {
        super.updateViews();
        String unit = entity.attributes.getString(Attribute.UNIT_OF_MEASUREMENT);
        value.setText(entity.state.concat(unit != null ? " " + unit : ""));
    }
}