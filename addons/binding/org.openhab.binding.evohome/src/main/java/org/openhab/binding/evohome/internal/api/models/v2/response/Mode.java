/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the mode
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class Mode {

    @SerializedName("systemMode")
    private String systemMode;

    @SerializedName("canBePermanent")
    private boolean canBePermanent;

    @SerializedName("canBeTemporary")
    private boolean canBeTemporary;

    @SerializedName("timingMode")
    private String timingMode;

    @SerializedName("maxDuration")
    private String maxDuration;

    @SerializedName("timingResolution")
    private String timingResolution;

}