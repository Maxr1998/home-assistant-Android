package io.homeassistant.android.view.viewholders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import io.homeassistant.android.BaseActivity;
import io.homeassistant.android.R;
import io.homeassistant.android.api.Attribute;
import io.homeassistant.android.api.Domain;
import io.homeassistant.android.api.requests.ServiceRequest;

public class MediaPlayerViewHolder extends CameraViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private TextView artist;
    private ImageButton playPause;

    public MediaPlayerViewHolder(View itemView) {
        super(itemView);
        artist = itemView.findViewById(R.id.media_artist);
        ImageButton previous = itemView.findViewById(R.id.media_previous);
        playPause = itemView.findViewById(R.id.media_play_pause);
        ImageButton next = itemView.findViewById(R.id.media_next);

        previous.setOnClickListener(this);
        playPause.setOnClickListener(this);
        playPause.setOnLongClickListener(this);
        next.setOnClickListener(this);
    }

    @Override
    protected void updateViews() {
        super.updateViews();
        name.setText(entity.attributes.getString(Attribute.MEDIA_TITLE));
        artist.setText(artist.getResources().getString(R.string.media_player_byline_format,
                entity.attributes.getString(Attribute.MEDIA_ARTIST), entity.getFriendlyName()));
        playPause.setImageResource(entity.state.equals("playing") ? R.drawable.ic_pause_24dp : R.drawable.ic_play_24dp);
    }

    @Override
    public void onClick(View v) {
        String action;
        switch (v.getId()) {
            case R.id.media_previous:
                action = "media_previous_track";
                break;
            case R.id.media_play_pause:
                action = "media_play_pause";
                break;
            case R.id.media_next:
                action = "media_next_track";
                break;
            default:
                return;
        }
        ((BaseActivity) v.getContext()).send(new ServiceRequest(Domain.MEDIA_PLAYER, action, entity.id), null);
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(v.getContext(), R.string.media_player_stop_toast, Toast.LENGTH_SHORT).show();
        ((BaseActivity) v.getContext()).send(new ServiceRequest(Domain.MEDIA_PLAYER, "media_stop", entity.id), null);
        return true;
    }
}