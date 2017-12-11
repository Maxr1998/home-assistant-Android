package io.homeassistant.android.view.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Map;

import io.homeassistant.android.R;
import io.homeassistant.android.api.websocket.requests.HassRequest;
import io.homeassistant.android.api.websocket.results.Entity;


public class BaseViewHolder extends RecyclerView.ViewHolder {

    public interface RequestResultListener
    {
        void onResult(boolean sucess, Object result);
    }

    public interface RequestSender
    {
        void send(HassRequest request, RequestResultListener listener);
    }

    protected Entity entity;
    protected Map<String,Entity> allEntities;
    final protected RequestSender sender;
    private final View space;

    public BaseViewHolder(View itemView, RequestSender sender) {
        super(itemView);
        this.sender = sender;
        space = itemView.findViewById(R.id.spacer);
    }

    public final void bind(Entity e, Map<String,Entity> allEntities, boolean isInGroup) {
        this.entity = e;
        this.allEntities = allEntities;
        //entity.registerObserver(this);
        updateViews();
        if(space != null)
        {
            space.setVisibility(isInGroup ? View.GONE : View.VISIBLE);
        }
    }

    protected void updateViews() {
        // Empty
    }

    public final void notifyChanged() {
        updateViews();
    }
}