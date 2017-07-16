package io.homeassistant.android.wear

import android.util.Pair
import io.homeassistant.android.api.HassUtils
import io.homeassistant.android.api.results.Entity
import io.homeassistant.android.view.ViewAdapter
import io.homeassistant.android.view.viewholders.BaseViewHolder

class WearViewAdapter : ViewAdapter.ChildViewAdapter() {

    private val entities = ArrayList<Entity>()

    fun updateEntities(entityMap: Map<String, Entity>) {
        val groups = ArrayList<Pair<Entity, List<Entity>>>()
        HassUtils.extractGroups(entityMap, groups, false)
        groups.forEach { group ->
            entities.add(group.first)
            entities.addAll(group.second)
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(entities[position])
    }

    override fun getItemViewType(position: Int): Int {
        return entities[position].type.ordinal
    }

    override fun getItemCount(): Int {
        return entities.size
    }
}