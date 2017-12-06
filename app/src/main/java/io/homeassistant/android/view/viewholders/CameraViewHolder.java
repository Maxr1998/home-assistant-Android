package io.homeassistant.android.view.viewholders;

import android.view.View;
import android.widget.ImageView;

import io.homeassistant.android.R;
import io.homeassistant.android.api.ImageUtils;


public class CameraViewHolder extends TextViewHolder {

    private final ImageView cameraView;

    public CameraViewHolder(View itemView, RequestSender sender) {
        super(itemView,sender);
        cameraView = (ImageView) itemView.findViewById(R.id.camera_view);
    }

    @Override
    protected void updateViews() {
        super.updateViews();
        try {
            ImageUtils.getInstance(name.getContext()).loadEntityDrawable(name.getContext(), entity, false, (drawable, async) -> {
                if (async) cameraView.post(() -> cameraView.setImageDrawable(drawable));
                else cameraView.setImageDrawable(drawable);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}