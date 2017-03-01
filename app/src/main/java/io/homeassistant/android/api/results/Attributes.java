package io.homeassistant.android.api.results;

import com.afollestad.ason.AsonName;

import java.util.List;

public class Attributes {
    @AsonName(name = "entity_id")
    public String[] children = null;
    public String friendly_name;
    public boolean hidden = false;
    public String icon;
    public List<String> options;
    public int supported_features;
    public String unit_of_measurement;
}
