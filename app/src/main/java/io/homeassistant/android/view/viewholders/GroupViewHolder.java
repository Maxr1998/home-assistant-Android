package io.homeassistant.android.view.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import io.homeassistant.android.BuildConfig;
import io.homeassistant.android.R;
import io.homeassistant.android.view.ViewAdapter;

public class GroupViewHolder extends TextViewHolder {

    public final RecyclerView childRecycler;
    public final ViewAdapter.ChildViewAdapter adapter;
    public final View space;

    public GroupViewHolder(View itemView) {
        super(itemView);
        if (BuildConfig.WEAR_APP) {
            childRecycler = null;
            adapter = null;
        } else {
            childRecycler = itemView.findViewById(R.id.childRecycler);
            adapter = new ViewAdapter.ChildViewAdapter();
            childRecycler.setAdapter(adapter);
        }
        space = itemView.findViewById(R.id.spacer);
    }
}