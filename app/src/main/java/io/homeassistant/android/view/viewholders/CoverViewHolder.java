package io.homeassistant.android.view.viewholders;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import io.homeassistant.android.HassActivity;
import io.homeassistant.android.R;
import io.homeassistant.android.api.requests.ToggleRequest;
import io.homeassistant.android.api.results.Entity;

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
    public void setEntity(Entity e) {
        super.setEntity(e);
        setImageButtonEnabled(itemView.getContext(), e.state.equalsIgnoreCase("open"), buttonCoverDown, R.drawable.ic_arrow_downward_24dp);
        setImageButtonEnabled(itemView.getContext(), e.state.equalsIgnoreCase("closed"), buttonCoverUp, R.drawable.ic_arrow_upward_24dp);
    }

    /**
     * Sets the specified image button to the given state, while modifying or
     * "graying-out" the icon as well
     *
     * @param enabled The state of the menu item
     * @param item The menu item to modify
     * @param iconResId The icon ID
     */
    public static void setImageButtonEnabled(Context ctxt, boolean enabled, ImageButton item,
                                             int iconResId) {
        item.setEnabled(enabled);
        Drawable originalIcon = ContextCompat.getDrawable(ctxt, iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);
    }

    /**
     * Mutates and applies a filter that converts the given drawable to a Gray
     * image. This method may be used to simulate the color of disable icons in
     * Honeycomb's ActionBar.
     *
     * @return a mutated version of the given drawable with a color filter
     *         applied.
     */
    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        return res;
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