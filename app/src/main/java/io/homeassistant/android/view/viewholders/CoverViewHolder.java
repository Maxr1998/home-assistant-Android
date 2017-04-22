package io.homeassistant.android.view.viewholders;

import android.view.View;
import android.widget.ImageButton;

import io.homeassistant.android.HassActivity;
import io.homeassistant.android.R;
import io.homeassistant.android.api.requests.ToggleRequest;

import static io.homeassistant.android.Utils.setStatefulImageButtonIcon;

public class CoverViewHolder extends TextViewHolder implements View.OnClickListener {

    private final ImageButton buttonCoverUp;
    private final ImageButton buttonCoverDown;
    private final ImageButton buttonCoverStop;

    public CoverViewHolder(View itemView) {
        super(itemView);
        buttonCoverUp = (ImageButton) itemView.findViewById(R.id.cover_up);
        buttonCoverUp.setOnClickListener(this);
        buttonCoverDown = (ImageButton) itemView.findViewById(R.id.cover_down);
        buttonCoverDown.setOnClickListener(this);
        buttonCoverStop = (ImageButton) itemView.findViewById(R.id.cover_stop);
        buttonCoverStop.setOnClickListener(this);
    }

    @Override
    protected void updateViews() {
        super.updateViews();
        setStatefulImageButtonIcon(itemView.getContext(), entity.state.equalsIgnoreCase("open"), buttonCoverDown, R.drawable.ic_arrow_downward_24dp);
        setStatefulImageButtonIcon(itemView.getContext(), entity.state.equalsIgnoreCase("closed"), buttonCoverUp, R.drawable.ic_arrow_upward_24dp);
    }

    @Override
    public void onClick(View v) {
        HassActivity activity = (HassActivity) v.getContext();
        String operation;
        switch (v.getId()) {
            case R.id.cover_up:
                operation = "open_cover";
                break;

            case R.id.cover_down:
                operation = "close_cover";
                break;

            case R.id.cover_stop:
                operation = "stop_cover";
                break;
            default:
                operation = "";
                break;
        }
        activity.send(new ToggleRequest(entity, operation), null);
    }
}