package io.homeassistant.android.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import io.homeassistant.android.R;
import io.homeassistant.android.ViewAdapter;

public class GroupViewHolder extends TextViewHolder {

    public final RecyclerView childRecycler;
    public final ViewAdapter.ChildViewAdapter adapter = new ViewAdapter.ChildViewAdapter();
    public final View space;

    public GroupViewHolder(View itemView) {
        super(itemView);
        childRecycler = (RecyclerView) itemView.findViewById(R.id.childRecycler);
        childRecycler.setAdapter(adapter);
        space = itemView.findViewById(R.id.spacer);
    }
}