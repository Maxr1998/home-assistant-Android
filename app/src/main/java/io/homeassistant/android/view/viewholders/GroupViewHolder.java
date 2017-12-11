package io.homeassistant.android.view.viewholders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.List;

import io.homeassistant.android.BuildConfig;
import io.homeassistant.android.R;
import io.homeassistant.android.api.websocket.results.Entity;
import io.homeassistant.android.view.adapter.EntityAdapter;
import io.homeassistant.android.view.adapter.EntityList;

public class GroupViewHolder extends TextViewHolder {

    final static String TAG = GroupViewHolder.class.getSimpleName();
    private final RecyclerView childRecycler;
    private final EntityAdapter adapter;
    //private final View space;

    public GroupViewHolder(View itemView, RecyclerView.RecycledViewPool pool, RequestSender sender) {
        super(itemView,sender);
        if (BuildConfig.WEAR_APP) {
            childRecycler = null;
            adapter = null;
        } else {
            childRecycler = itemView.findViewById(R.id.childRecycler);
            childRecycler.setRecycledViewPool(pool);
            adapter = new EntityAdapter(sender, pool);
            adapter.setIsGroup(true);
            childRecycler.setAdapter(adapter);
        }

    }

    @Override
    protected void updateViews() {
        super.updateViews();

        // Get the child views
        try {
            List<Entity> children = entity.getGroupChildren(allEntities);

            EntityList list = new EntityList(children,allEntities);
            adapter.setEntities(list);

        } catch (Exception e) {
            Log.e(TAG,"Group holder is not holding a group.");
        }

        // TODO reinstate this behaviour
        //holder.space.setVisibility(position >= getItemCount() - holder.itemView.getResources().getInteger(R.integer.view_columns) ? View.GONE : View.VISIBLE);*/

    }
}