package io.homeassistant.android.api;

import android.support.annotation.NonNull;

import com.afollestad.ason.Ason;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.homeassistant.android.api.results.Entity;
import io.homeassistant.android.api.results.RequestResult;

public final class HassUtils {

    private HassUtils() {
    }

    /**
     * Extract entities from a RequestResult of a StateRequest
     *
     * @param result    the RequestResult to extract from
     * @param entityMap the Map where entities are store with their id as key
     * @return true if items were added, else false
     */
    public static boolean extractEntitiesFromStateResult(@NonNull RequestResult result, @NonNull Map<String, Entity> entityMap) {
        if (result.success && result.result != null && result.result.getClass().isArray()) {
            // Clear map before adding new content
            entityMap.clear();
            for (Object o : (Object[]) result.result) {
                Entity entity = Ason.deserialize((Ason) o, Entity.class);
                entityMap.put(entity.id, entity);
            }
            return true;
        }
        return false;
    }

    public static boolean extractGroups(@NonNull Map<String, Entity> entityMap, List<Entity> entities) {
        entities.clear();
        Iterator<String> iterator = entityMap.keySet().iterator();
        while (iterator.hasNext()) {
            String entityId = iterator.next();
            if (extractDomainFromEntityId(entityId).equals("group")) {
                Entity entity = entityMap.get(entityId);
                if (entity.attributes.hidden) {
                    continue;
                }
                // Add visible group item
                entity.type = EntityType.GROUP;
                entities.add(entity);

                // Search and add items from group
                String[] children = entity.attributes.children;
                for (int i = 0; i < children.length; i++) {
                    Entity child = entityMap.get(children[i]);
                    child.type = extractTypeFromEntity(child);
                    if ((child.attributes == null) ||
                            ((child.attributes != null) && (!child.attributes.hidden)))
                    {
                        entities.add(child);
                    }
                }

                // Add spacer
                Entity spacer = new Entity();
                spacer.type = EntityType.SPACER;
                entities.add(spacer);
            }
        }
        return false;
    }

    public static String extractDomainFromEntityId(String entityId) {
        return entityId.split("\\.")[0];
    }

    public static EntityType extractTypeFromEntity(Entity entity) {
        String domain = extractDomainFromEntityId(entity.id);
        switch (domain) {
            case "automation":
            case "light":
            case "switch":
            case "input_boolean":
                return EntityType.SWITCH;
            case "sensor":
            case "binary_sensor":
                return EntityType.SENSOR;
            case "scene":
                return EntityType.SCENE;
            case "input_select":
                return EntityType.INPUT_SELECT;
            default:
                if (entity.attributes != null && entity.attributes.friendly_name != null) {
                    return EntityType.TEXT;
                }
                return EntityType.BASE;
        }
    }
}