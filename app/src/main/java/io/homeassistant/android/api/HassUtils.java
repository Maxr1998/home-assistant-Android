package io.homeassistant.android.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.afollestad.ason.Ason;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.homeassistant.android.api.results.Entity;
import io.homeassistant.android.api.results.EventData;

import static io.homeassistant.android.api.Domain.AUTOMATION;
import static io.homeassistant.android.api.Domain.BINARY_SENSOR;
import static io.homeassistant.android.api.Domain.CAMERA;
import static io.homeassistant.android.api.Domain.COVER;
import static io.homeassistant.android.api.Domain.DEVICE_TRACKER;
import static io.homeassistant.android.api.Domain.GROUP;
import static io.homeassistant.android.api.Domain.INPUT_BOOLEAN;
import static io.homeassistant.android.api.Domain.INPUT_SELECT;
import static io.homeassistant.android.api.Domain.LIGHT;
import static io.homeassistant.android.api.Domain.LOCK;
import static io.homeassistant.android.api.Domain.SCENE;
import static io.homeassistant.android.api.Domain.SENSOR;
import static io.homeassistant.android.api.Domain.SUN;
import static io.homeassistant.android.api.Domain.SWITCH;

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
    public static boolean extractEntitiesFromStateResult(@Nullable Object result, @NonNull Map<String, Entity> entityMap) {
        if (result != null && result.getClass().isArray()) {
            // Clear map before adding new content
            entityMap.clear();
            for (Object o : (Object[]) result) {
                Entity entity = Ason.deserialize((Ason) o, Entity.class);
                entityMap.put(entity.id, entity);
            }
            return true;
        }
        return false;
    }

    /**
     * Extract an updated entity from an event and update the entityMap with the new state
     *
     * @param result    the EventData to extract from
     * @param entityMap the Map where entities are store with their id as key
     * @return true if items were updated, else false
     */
    public static boolean updateEntityFromEventResult(@Nullable EventData result, @NonNull Map<String, Entity> entityMap) {
        if (result != null) {
            entityMap.put(result.entity_id, result.new_state);
            return true;
        }
        return false;
    }

    public static void extractGroups(@NonNull Map<String, Entity> entityMap, List<Pair<Entity, List<Entity>>> entities) {
        entities.clear();
        for (String entityId : entityMap.keySet()) {
            if (Entity.getDomain(entityId).equals(GROUP)) {
                Entity entity = entityMap.get(entityId);
                if (entity.attributes.hidden) {
                    continue;
                }
                // Add visible group item
                entity.type = EntityType.GROUP;

                // Add group children
                List<Entity> children = new ArrayList<>();
                String[] childrenKeys = entity.attributes.children;
                for (int i = 0; i < childrenKeys.length; i++) {
                    Entity child = entityMap.get(childrenKeys[i]);
                    if (child == null) continue;
                    child.type = extractTypeFromEntity(child);
                    if (!child.attributes.hidden) {
                        children.add(child);
                    }
                }

                entities.add(new Pair<>(entity, children));
            }
        }

        // Sort groups according to their order number
        //noinspection Java8ListSort,ComparatorCombinators
        Collections.sort(entities, (o1, o2) -> o1.first.compareTo(o2.first));
    }

    @NonNull
    public static EntityType extractTypeFromEntity(@NotNull Entity e) {
        switch (e.getDomain()) {
            case AUTOMATION:
            case INPUT_BOOLEAN:
            case LIGHT:
            case LOCK:
            case SWITCH:
                return EntityType.SWITCH;
            case BINARY_SENSOR:
            case DEVICE_TRACKER:
            case SENSOR:
            case SUN:
                return EntityType.SENSOR;
            case CAMERA:
                return EntityType.CAMERA;
            case COVER:
                return EntityType.COVER;
            case INPUT_SELECT:
                return EntityType.INPUT_SELECT;
            case SCENE:
                return EntityType.SCENE;
            default:
                if (e.attributes.friendly_name != null) {
                    return EntityType.TEXT;
                }
                return EntityType.BASE;
        }
    }

    @NonNull
    public static String extractEntityName(@NonNull Entity e) {
        return e.attributes.friendly_name != null ? e.attributes.friendly_name : e.id;
    }

    @Nullable
    public static String getOnState(@NonNull Entity e, boolean on) {
        if (extractTypeFromEntity(e) == EntityType.SWITCH) {
            if (e.getDomain().equals(LOCK)) {
                return on ? "locked" : "unlocked";
            } else {
                return on ? "on" : "off";
            }
        } else return null;
    }

    public static void applyDefaultIcon(@NotNull Entity e) {
        if (e.attributes.icon != null || e.attributes.entity_picture != null)
            return;
        String icon;
        // For now, include all domains from https://github.com/home-assistant/home-assistant-polymer/blob/master/src/util/hass-util.html#L219,
        // even though most are currently not supported by this app.
        switch (e.getDomain()) {
            case "alarm_control_panel":
                icon = e.state != null && e.state.equals("disarmed") ? "mdi:bell-outline" : "mdi:bell";
                break;
            case AUTOMATION:
                icon = "mdi:playlist-play";
                break;
            case BINARY_SENSOR:
                icon = e.state != null && e.state.equals("off") ? "mdi:radiobox-blank" : "mdi:checkbox-marked-circle";
                break;
            case "calendar":
                icon = "mdi:calendar";
                break;
            case CAMERA:
                icon = "mdi:video";
                break;
            case "climate":
                icon = "mdi:nest-thermostat";
                break;
            case "configurator":
                icon = "mdi:settings";
                break;
            case "conversation":
                icon = "mdi:text-to-speech";
                break;
            case COVER:
                icon = e.state != null && e.state.equals("open") ? "mdi:window-open" : "mdi:window-closed";
                break;
            case "device_tracker":
                icon = "mdi:account";
                break;
            case "fan":
                icon = "mdi:fan";
                break;
            case GROUP:
                icon = "mdi:google-circles-communities";
                break;
            case "homeassistant":
                icon = "mdi:home";
                break;
            case "image_processing":
                icon = "mdi:image-filter-frames";
                break;
            case INPUT_BOOLEAN:
                icon = "mdi:drawing";
                break;
            case INPUT_SELECT:
                icon = "mdi:format-list-bulleted";
                break;
            case "input_slider":
                icon = "mdi:ray-vertex";
                break;
            case LIGHT:
                icon = "mdi:lightbulb";
                break;
            case LOCK:
                icon = e.state != null && e.state.equals("unlocked") ? "mdi:lock-open" : "mdi:lock";
                break;
            case "media_player":
                icon = e.state != null && !e.state.equals("off") && !e.state.equals("idle") ? "mdi:cast-connected" : "mdi:cast";
                break;
            case "notify":
                icon = "mdi:comment-alert";
                break;
            case "proximity":
                icon = "mdi:apple-safari";
                break;
            case "remote":
                icon = "mdi:remote";
                break;
            case SCENE:
                icon = "mdi:google-pages";
                break;
            case "script":
                icon = "mdi:file-document";
                break;
            case SENSOR:
                icon = "mdi:eye";
                break;
            case "simple_alarm":
                icon = "mdi:bell";
                break;
            case SUN:
                icon = "mdi:white-balance-sunny";
                e.state = e.state.replace('_', ' ');
                break;
            case SWITCH:
                icon = "mdi:flash";
                break;
            case "updater":
                icon = "mdi:cloud-upload";
                break;
            case "weblink":
                icon = "mdi:open-in-new";
                break;
            default:
                icon = null;
                break;
        }
        e.attributes.icon = icon;
    }
}