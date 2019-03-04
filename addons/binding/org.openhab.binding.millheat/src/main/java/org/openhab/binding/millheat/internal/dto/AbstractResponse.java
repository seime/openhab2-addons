package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

public abstract class AbstractResponse {
    public String errorCode;

    @SerializedName("error")
    public String errorName;

    @SerializedName("description")
    public String errorDescription;

}
