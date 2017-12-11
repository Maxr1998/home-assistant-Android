package io.homeassistant.android.view.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.homeassistant.android.api.Attribute;
import io.homeassistant.android.api.HassUtils;
import io.homeassistant.android.api.websocket.results.Entity;

/**
 * Created by Nicolas on 2017-12-06.
 */

public class EntityList {
    final private List<Entity> items;
    final private Map<String,Entity> allEntities;

    public EntityList(List<Entity> items, Map<String, Entity> allEntities) {
        this.items = items;
        this.allEntities = allEntities;
    }

    public Entity get(int position)
    {
        return items.get(position);
    }

    public int size()
    {
        return items.size();
    }

    public Entity find(String id)
    {
        return allEntities.get(id);
    }

    public Map<String,Entity> getAllEntities() {
        return allEntities;
    }

    public static EntityList getTopLevelEntities(List<Entity> entities) {

        Map<String, Entity> toplevel = HassUtils.createEntityMap(entities);

        for(  Entity e: entities )
        {
            if(e.getType()==Entity.TYPE_GROUP)
            {
                for( String id : e.getGroupChildren())
                {
                    toplevel.remove(id);
                }
            }
        }

        return new EntityList(new ArrayList<>(toplevel.values()),HassUtils.createEntityMap(entities));
    }

    public static EntityList getTopLevelEntities(Map<String, Entity> entities) {

        HashMap<String, Entity> toplevel = new HashMap<>(entities);

        for(  Entity e: entities.values() )
        {

            if(e.getBoolAttribute(Attribute.HIDDEN,false))
            {
                toplevel.remove(e.id);
            }
            else
            {
                if(e.getType()==Entity.TYPE_GROUP)
                {
                    for( String id : e.getGroupChildren())
                    {
                        toplevel.remove(id);
                    }
                }
            }
        }

        return new EntityList(new ArrayList<>(toplevel.values()),entities);
    }
}
