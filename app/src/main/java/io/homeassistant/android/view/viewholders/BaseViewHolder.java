package io.homeassistant.android.view.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

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
    final protected RequestSender sender;

    public BaseViewHolder(View itemView, RequestSender sender) {
        super(itemView);
        this.sender = sender;
    }

    public final void bind(Entity e) {
        entity = e;
        //entity.registerObserver(this);
        updateViews();
    }

    protected void updateViews() {
        // Empty
    }

    public final void notifyChanged() {
        updateViews();
    }
}