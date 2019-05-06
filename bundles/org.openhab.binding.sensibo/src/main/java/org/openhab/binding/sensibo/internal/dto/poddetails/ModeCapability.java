package org.openhab.binding.sensibo.internal.dto.poddetails;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class ModeCapability {
    @SerializedName("swing")
    private List<String> swingModes;
    private Map<String, Temperature> temperatures;
    private List<String> fanLevels;

    public List<String> getSwingModes() {
        return swingModes;
    }

    public Map<String, Temperature> getTemperatures() {
        return temperatures;
    }

    public List<String> getFanLevels() {
        return fanLevels;
    }
}
