package io.homeassistant.android.view.viewholders;

import android.view.View;
import android.widget.ImageView;

import io.homeassistant.android.R;
import io.homeassistant.android.api.icons.ImageUtils;


public class CameraViewHolder extends TextViewHolder {

    private final ImageView imageView;

    public CameraViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_view);
    }

    @Override
    protected void updateViews() {
        super.updateViews();
        try {
            ImageUtils.getInstance(name.getContext()).getEntityDrawable(name.getContext(), entity, (drawable, async) -> {
                if (async) imageView.post(() -> imageView.setImageDrawable(drawable));
                else imageView.setImageDrawable(drawable);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}