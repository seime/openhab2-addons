package org.openhab.binding.sensibo.internal.dto.poddetails;

import com.google.gson.annotations.SerializedName;

public class ConnectionStatus {
    @SerializedName("isAlive")
    private boolean alive;

    public boolean isAlive() {
        return alive;
    }

}
