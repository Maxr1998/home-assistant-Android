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
import java.util.Map;

import io.homeassistant.android.api.EntityType;
import io.homeassistant.android.view.viewholders.BaseViewHolder;

import static io.homeassistant.android.api.Domain.AUTOMATION;
import static io.homeassistant.android.api.Domain.BINARY_SENSOR;
import static io.homeassistant.android.api.Domain.CAMERA;
import static io.homeassistant.android.api.Domain.CLIMATE;
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

public class Entity implements Comparable<Entity> {
    @AsonIgnore
    private final List<WeakReference<BaseViewHolder>> observers = new ArrayList<>();
    @AsonIgnore
    public EntityType type = EntityType.BASE;

    @AsonName(name = "entity_id")
    public String id;
    public String last_changed;
    public String last_updated;
    public String state = null;

    public Ason attributes;

    @Override
    public String toString() {
        return String.format("%1$s[%2$s]", type, id);
    }

    public boolean isHidden() {
        return (attributes.has("hidden") && (((Boolean)attributes.get("hidden")).booleanValue() == true));
    }

    public String getFriendlyName(){
        return attributes.has("friendly_name") ? (String)attributes.get("friendly_name") : this.id;
    }

    @Override
    public int compareTo(@NonNull Entity e) {
        if ((int)attributes.getInt("order",-1) != -1 && (int)e.attributes.getInt("order",-1) != -1) {
            return (int)attributes.getInt("order",-1) - (int)e.attributes.getInt("order",-1);
        } else if (attributes.get("friendly_name") != null && e.attributes.get("friendly_name") != null) {
            return ((String)attributes.get("friendly_name")).compareToIgnoreCase((String)e.attributes.get("friendly_name"));
        }
        return id.compareToIgnoreCase(e.id);
    }

    public String getDomain() {
        return id.split("\\.")[0];
    }

    public void applyType() {
        switch (getDomain()) {
            case AUTOMATION:
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
            case SCENE:
                type = EntityType.SCENE;
                break;
            default:
                if (attributes.get("friendly_name") != null) {
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