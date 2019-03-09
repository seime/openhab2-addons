package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

public abstract class AbstractResponse {

    public static final int ERROR_CODE_ACCESS_TOKEN_EXPIRED = 3515;

    public int errorCode;

    @SerializedName("error")
    public String errorName;

    @SerializedName("description")
    public String errorDescription;

}
