package io.homeassistant.android.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import com.afollestad.ason.Ason;
import com.afollestad.ason.AsonArray;
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
                entity.applyType();
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
    @Nullable
    public static Entity updateEntityFromEventResult(@Nullable EventData result, @NonNull Map<String, Entity> entityMap) {
        if (result != null) {
            Entity current = entityMap.get(result.entity_id);
            Entity updated = result.new_state;
            current.state = updated.state;
            current.attributes = updated.attributes;
            current.last_changed = updated.last_changed;
            current.last_updated = updated.last_updated;
            return current;
        }
        return null;
    }

    public static void extractGroups(@NonNull Map<String, Entity> entityMap, List<Pair<Entity, List<Entity>>> entities) {
        entities.clear();
        for (Entity entity : entityMap.values()) {
            if (entity.getDomain().equals(GROUP)) {
                if (entity.isHidden()) {
                    continue;
                }

                // Add group children
                List<Entity> children = new ArrayList<>();
                AsonArray entity_ids = (AsonArray) entity.attributes.get("entity_id");
                for (Object childrenKey : entity_ids.toList()) {
                    Entity child = entityMap.get((String)childrenKey);
                    if (child == null) continue;
                    if (!child.isHidden()) {
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

    @Nullable
    public static String getOnState(@NonNull Entity e, boolean on) {
        if (e.type == EntityType.SWITCH) {
            if (e.getDomain().equals(LOCK)) {
                return on ? "locked" : "unlocked";
            } else {
                return on ? "on" : "off";
            }
        } else return null;
    }

    public static void applyDefaultIcon(@NotNull Entity e) {
        if (e.attributes.get("icon") != null || e.attributes.get("entity_picture") != null)
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
            case DEVICE_TRACKER:
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
        e.attributes.put("icon",icon);
    }
}