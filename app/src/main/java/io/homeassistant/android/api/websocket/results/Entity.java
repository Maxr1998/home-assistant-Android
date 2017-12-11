package io.homeassistant.android.api.websocket.results;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.ason.Ason;
import com.afollestad.ason.AsonIgnore;
import com.afollestad.ason.AsonName;


import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.homeassistant.android.api.Attribute;


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
import static io.homeassistant.android.api.Domain.SCRIPT;
import static io.homeassistant.android.api.Domain.SENSOR;
import static io.homeassistant.android.api.Domain.SUN;
import static io.homeassistant.android.api.Domain.SWITCH;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public class Entity implements Comparable<Entity> {

    //@AsonIgnore
    //private final List<WeakReference<BaseViewHolder>> observers = new ArrayList<>();

    @Retention(SOURCE)
    @IntDef({TYPE_BASE,
            TYPE_CAMERA,
            TYPE_CLIMATE,
            TYPE_COVER,
            TYPE_GROUP,
            TYPE_INPUT_SELECT,
            TYPE_SENSOR,
            TYPE_SCENE,
            TYPE_SWITCH,
            TYPE_TEXT})
    public @interface Type {}
    public static final int  TYPE_BASE = 0;
    public static final int  TYPE_CAMERA =2;
    public static final int  TYPE_CLIMATE =3;
    public static final int  TYPE_COVER =4;
    public static final int  TYPE_GROUP =5;
    public static final int  TYPE_INPUT_SELECT =6;
    public static final int  TYPE_SENSOR =7;
    public static final int  TYPE_SCENE =8;
    public static final int  TYPE_SWITCH =9;
    public static final int  TYPE_TEXT =10;

    @AsonIgnore
    //public EntityType type = EntityType.BASE;
    private @Type int type;

    public @Type int getType()
    {
        return type;
    }

    @AsonName(name = Attribute.ENTITY_ID)
    public String id;
    public String last_changed;
    public String last_updated;
    public String state = null;

    public Ason attributes;

    @Override
    public String toString() {
        return String.format("[%1$s] %2$s %3$s", type, id, getFriendlyName());
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
            case INPUT_BOOLEAN:
            case LIGHT:
            case LOCK:
            case SWITCH:
                type = TYPE_SWITCH;
                break;
            case BINARY_SENSOR:
            case DEVICE_TRACKER:
            case SENSOR:
            case SUN:
                type = TYPE_SENSOR;
                break;
            case CAMERA:
                type = TYPE_CAMERA;
                break;
            case CLIMATE:
                type = TYPE_CLIMATE;
                break;
            case COVER:
                type = TYPE_COVER;
                break;
            case GROUP:
                type = TYPE_GROUP;
                break;
            case INPUT_SELECT:
                type = TYPE_INPUT_SELECT;
                break;
            case SCENE:
            case SCRIPT:
                type = TYPE_SCENE;
                break;
            default:
                if (attributes.get(Attribute.FRIENDLY_NAME) != null) {
                    type = TYPE_TEXT;
                }
        }
    }

    public @Nullable  List<String> getGroupChildren()  {
        if( type != TYPE_GROUP)
            return null;

        return attributes.getList(Attribute.ENTITY_ID, String.class);
    }

    public @Nullable List<Entity> getGroupChildren(Map<String,Entity> allEntities) throws Exception {
        List<String> ids = getGroupChildren();
        if(ids==null)
            return null;

        List<Entity> entities = new ArrayList<>();
        for(String id : ids)
        {
            Entity e = allEntities.get(id);
            if(e!=null)
                entities.add(e);
        }

        return entities;
    }

    public boolean getBoolAttribute(String name, boolean defaultValue)
    {
        return attributes.getBool(name,defaultValue);
    }

    /*public void registerObserver(BaseViewHolder observer) {
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
    }*/
}