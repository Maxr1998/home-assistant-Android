package io.homeassistant.android.view.viewholders;

import android.support.v7.widget.SwitchCompat;
import android.view.View;
import io.homeassistant.android.HassActivity;
import io.homeassistant.android.R;
import io.homeassistant.android.api.requests.ToggleRequest;
import io.homeassistant.android.api.results.Entity;
import io.homeassistant.android.api.results.RequestResult;

public class LockViewHolder extends TextViewHolder implements View.OnClickListener {

    private final SwitchCompat stateSwitch;

    public LockViewHolder(View itemView) {
        super(itemView);
        stateSwitch = (SwitchCompat) itemView.findViewById(R.id.state_switch);
        stateSwitch.setOnClickListener(null);
    }

    @Override
    public void setEntity(Entity e) {
        super.setEntity(e);
        stateSwitch.setChecked(entity.state.equals("locked"));
        stateSwitch.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        HassActivity activity = (HassActivity) v.getContext();
        String operation = stateSwitch.isChecked() ? "lock" : "unlock";
        activity.send(new ToggleRequest(entity, operation), new RequestResult.OnRequestResultListener() {
            @Override
            public void onRequestResult(boolean success, Object result) {
                if (success) {
                    entity.state = stateSwitch.isChecked() ? "locked" : "unlocked";
                } else {
                    stateSwitch.toggle();
                }
            }
        });
    }


}