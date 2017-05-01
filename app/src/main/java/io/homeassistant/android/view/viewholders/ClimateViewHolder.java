package io.homeassistant.android.view.viewholders;

import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import io.homeassistant.android.R;
import io.homeassistant.android.api.Attribute;

public class ClimateViewHolder extends TextViewHolder {

    private final TextView operation;
    private final TextView targetTemperature;
    private final TextView currentTemperature;

    public ClimateViewHolder(View itemView) {
        super(itemView);
        operation = (TextView) itemView.findViewById(R.id.operation);
        targetTemperature = (TextView) itemView.findViewById(R.id.targetTemperature);
        currentTemperature = (TextView) itemView.findViewById(R.id.currentTemperature);
    }

    @Override
    protected void updateViews() {
        super.updateViews();
        operation.setText(entity.attributes.getString(Attribute.OPERATION_MODE));
        targetTemperature.setText(String.format(Locale.getDefault(), "%1$.1f %2$s",
                entity.attributes.get(Attribute.TEMPERATURE, (Number) 0).doubleValue(), entity.attributes.getString(Attribute.UNIT_OF_MEASUREMENT)));
        currentTemperature.setText(itemView.getResources().getString(R.string.climate_current_temperature,
                entity.attributes.get(Attribute.CURRENT_TEMPERATURE, (Number) 0).doubleValue(), entity.attributes.getString(Attribute.UNIT_OF_MEASUREMENT)));
    }
}