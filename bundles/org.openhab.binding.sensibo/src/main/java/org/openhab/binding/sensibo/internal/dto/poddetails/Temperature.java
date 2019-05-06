package org.openhab.binding.sensibo.internal.dto.poddetails;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Temperature {
    private boolean isNative;
    @SerializedName("values")
    private List<Integer> validValues;

    public boolean isNative() {
        return isNative;
    }

    public List<Integer> getValidValues() {
        return validValues;
    }
}
