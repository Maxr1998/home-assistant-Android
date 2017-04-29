package io.homeassistant.android.view.viewholders;

import android.view.View;
import android.widget.TextView;

import io.homeassistant.android.R;

public class ClimateViewHolder extends TextViewHolder  {

    private final TextView operation;
    private final TextView targetTemperature;
    private final TextView currentTemperature;

    public ClimateViewHolder(View itemView) {
        super(itemView);
        operation = (TextView)itemView.findViewById(R.id.operation);
        targetTemperature = (TextView)itemView.findViewById(R.id.targetTemperature);
        currentTemperature = (TextView)itemView.findViewById(R.id.currentTemperature);
    }

    @Override
    protected void updateViews() {
        super.updateViews();
        operation.setText(entity.attributes.getString("operation_mode"));
        targetTemperature.setText(entity.attributes.getInt("temperature") + " " + entity.attributes.getString("unit_of_measurement"));
        String currentTemp = itemView.getContext().getResources().getString(R.string.climate_current_temperature, entity.attributes.getInt("current_temperature"));
        currentTemperature.setText(currentTemp + " " + entity.attributes.getString("unit_of_measurement"));
    }
}
