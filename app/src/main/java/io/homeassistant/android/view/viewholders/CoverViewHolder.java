package io.homeassistant.android.view.viewholders;

import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import io.homeassistant.android.HassActivity;
import io.homeassistant.android.R;
import io.homeassistant.android.api.requests.ToggleRequest;
import io.homeassistant.android.api.results.RequestResult;

public class CoverViewHolder  extends TextViewHolder implements View.OnClickListener {

    private final AppCompatImageButton buttonCoverUp;
    private final AppCompatImageButton buttonCoverDown;
    private final AppCompatImageButton buttonCoverStop;

    public CoverViewHolder(View itemView) {
        super(itemView);
        buttonCoverUp = (AppCompatImageButton) itemView.findViewById(R.id.cover_up);
        buttonCoverUp.setOnClickListener(this);
        buttonCoverDown = (AppCompatImageButton) itemView.findViewById(R.id.cover_down);
        buttonCoverDown.setOnClickListener(this);
        buttonCoverStop = (AppCompatImageButton) itemView.findViewById(R.id.cover_stop);
        buttonCoverStop.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        HassActivity activity = (HassActivity) v.getContext();

        String operation = null;
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
                break;
        }


        activity.send(new ToggleRequest(entity, operation), new RequestResult.OnRequestResultListener() {
            @Override
            public void onRequestResult(boolean success, Object result) {
               // nothing to do
            }
        });
    }


}
