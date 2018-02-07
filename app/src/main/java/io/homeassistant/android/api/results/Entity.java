package io.homeassistant.android.api.results;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.afollestad.ason.Ason;
import com.afollestad.ason.AsonIgnore;
import com.afollestad.ason.AsonName;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.homeassistant.android.api.Attribute;
import io.homeassistant.android.api.EntityType;
import io.homeassistant.android.view.viewholders.BaseViewHolder;

import static io.homeassistant.android.api.Domain.AUTOMATION;
import static io.homeassistant.android.api.Domain.BINARY_SENSOR;
import static io.homeassistant.android.api.Domain.CAMERA;
import static io.homeassistant.android.api.Domain.CLIMATE;
import static io.homeassistant.android.api.Domain.COVER;
import static io.homeassistant.android.api.Domain.DEVICE_TRACKER;
import static io.homeassistant.android.api.Domain.FAN;
import static io.homeassistant.android.api.Domain.GROUP;
import static io.homeassistant.android.api.Domain.INPUT_BOOLEAN;
import static io.homeassistant.android.api.Domain.INPUT_SELECT;
import static io.homeassistant.android.api.Domain.LIGHT;
import static io.homeassistant.android.api.Domain.LOCK;
import static io.homeassistant.android.api.Domain.MEDIA_PLAYER;
import static io.homeassistant.android.api.Domain.SCENE;
import static io.homeassistant.android.api.Domain.SCRIPT;
import static io.homeassistant.android.api.Domain.SENSOR;
import static io.homeassistant.android.api.Domain.SUN;
import static io.homeassistant.android.api.Domain.SWITCH;

public class Entity implements Comparable<Entity> {
    @AsonIgnore
    private final List<WeakReference<BaseViewHolder>> observers = new ArrayList<>();
    @AsonIgnore
    public EntityType type = EntityType.BASE;

    @AsonName(name = Attribute.ENTITY_ID)
    public String id;
    public String last_changed;
    public String last_updated;
    public String state = null;

    public Ason attributes;

    @Override
    public String toString() {
        return String.format("[%1$s] %2$s: %3$s", type, id, state);
    }

    public boolean isHidden() {
        return attributes.getBool(Attribute.HIDDEN);
    }

    public String getFriendlyName() {
        return attributes.has(Attribute.FRIENDLY_NAME) ? attributes.getString(Attribute.FRIENDLY_NAME) : id;
    }

    @Override
    public int compareTo(@NonNull Entity e) {
        if (attributes.getInt(Attribute.ORDER, -1) != -1 && e.attributes.getInt(Attribute.ORDER, -1) != -1) {
            return attributes.getInt(Attribute.ORDER) - e.attributes.getInt(Attribute.ORDER);
        } else if (attributes.has(Attribute.FRIENDLY_NAME) && e.attributes.has(Attribute.FRIENDLY_NAME)) {
            //noinspection ConstantConditions
            return (attributes.getString(Attribute.FRIENDLY_NAME)).compareToIgnoreCase(e.attributes.getString(Attribute.FRIENDLY_NAME));
        }
        return id.compareToIgnoreCase(e.id);
    }

    public String getDomain() {
        return id.split("\\.")[0];
    }

    public String getName() {
        return id.split("\\.")[1];
    }

    public void applyType() {
        switch (getDomain()) {
            case AUTOMATION:
            case FAN:
            case INPUT_BOOLEAN:
            case LIGHT:
            case LOCK:
            case SWITCH:
                type = EntityType.SWITCH;
                break;
            case BINARY_SENSOR:
            case DEVICE_TRACKER:
            case SENSOR:
            case SUN:
                type = EntityType.SENSOR;
                break;
            case CAMERA:
                type = EntityType.CAMERA;
                break;
            case CLIMATE:
                type = EntityType.CLIMATE;
                break;
            case COVER:
                type = EntityType.COVER;
                break;
            case GROUP:
                type = EntityType.GROUP;
                break;
            case INPUT_SELECT:
                type = EntityType.INPUT_SELECT;
                break;
            case MEDIA_PLAYER:
                type = EntityType.MEDIA_PLAYER;
                break;
            case SCENE:
            case SCRIPT:
                type = EntityType.SCENE;
                break;
            default:
                if (attributes.get(Attribute.FRIENDLY_NAME) != null) {
                    type = EntityType.TEXT;
                }
        }
    }

    public void registerObserver(BaseViewHolder observer) {
        observers.add(new WeakReference<>(observer));
    }

    public void notifyObservers() {
        Iterator<WeakReference<BaseViewHolder>> iterator = observers.iterator();
        while (iterator.hasNext()) {
            BaseViewHolder viewHolder = iterator.next().get();
            if (viewHolder != null) {
                if (viewHolder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    viewHolder.notifyChanged();
                    return;
                }
            }
            iterator.remove();
        }
    }
}