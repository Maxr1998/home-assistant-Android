package io.homeassistant.android.view.viewholders;

import android.view.View;
import android.widget.Button;

import io.homeassistant.android.R;
import io.homeassistant.android.api.websocket.requests.ToggleRequest;

public class SceneViewHolder extends TextViewHolder implements View.OnClickListener {

    private final Button sceneButton;

    public SceneViewHolder(View itemView, RequestSender sender) {
        super(itemView,sender);
        sceneButton = itemView.findViewById(R.id.scene_button);
    }

    @Override
    protected void updateViews() {
        super.updateViews();
        sceneButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        sender.send(new ToggleRequest(entity), null);
    }
}