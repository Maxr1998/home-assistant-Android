package io.homeassistant.android.view.viewholders;

import android.view.View;
import android.widget.ImageButton;

import io.homeassistant.android.BaseActivity;
import io.homeassistant.android.R;
import io.homeassistant.android.api.Attribute;
import io.homeassistant.android.api.requests.ToggleRequest;

import static io.homeassistant.android.Utils.setStatefulImageButtonIcon;

public class CoverViewHolder extends TextViewHolder implements View.OnClickListener {

    private static final int SUPPORT_OPEN = 1;
    private static final int SUPPORT_CLOSE = 2;
    private static final int SUPPORT_STOP = 8;

    private final ImageButton buttonCoverUp;
    private final ImageButton buttonCoverDown;
    private final ImageButton buttonCoverStop;

    public CoverViewHolder(View itemView) {
        super(itemView);
        buttonCoverUp = itemView.findViewById(R.id.cover_up);
        buttonCoverUp.setOnClickListener(this);
        buttonCoverDown = itemView.findViewById(R.id.cover_down);
        buttonCoverDown.setOnClickListener(this);
        buttonCoverStop = itemView.findViewById(R.id.cover_stop);
        buttonCoverStop.setOnClickListener(this);
    }


    @Override
    protected void updateViews() {
        super.updateViews();
        int supportedFeatures = entity.attributes.getInt(Attribute.SUPPORTED_FEATURES);
        buttonCoverUp.setVisibility(((supportedFeatures & SUPPORT_OPEN) != 0) ? View.VISIBLE : View.GONE);
        buttonCoverDown.setVisibility(((supportedFeatures & SUPPORT_CLOSE) != 0) ? View.VISIBLE : View.GONE);
        buttonCoverStop.setVisibility(((supportedFeatures & SUPPORT_STOP) != 0) ? View.VISIBLE : View.GONE);

        setStatefulImageButtonIcon(itemView.getContext(), entity.state.equalsIgnoreCase("open"), buttonCoverDown, R.drawable.ic_arrow_downward_24dp);
        setStatefulImageButtonIcon(itemView.getContext(), entity.state.equalsIgnoreCase("closed"), buttonCoverUp, R.drawable.ic_arrow_upward_24dp);
    }

    @Override
    public void onClick(View v) {
        BaseActivity activity = (BaseActivity) v.getContext();
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