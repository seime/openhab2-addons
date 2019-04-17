/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.millheat.internal.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.millheat.internal.model.MillheatModel;

/**
 * @author Arne Seime - Initial contribution
 */
public abstract class MillheatBaseThingHandler extends BaseThingHandler {
    public MillheatBaseThingHandler(Thing thing) {
        super(thing);
    }

    public void updateState(MillheatModel model) {
        for (Channel channel : getThing().getChannels()) {
            handleCommand(channel.getUID(), RefreshType.REFRESH, model);
        }
    }

    protected abstract void handleCommand(@NonNull ChannelUID uid, @NonNull Command command,
            @NonNull MillheatModel model);
}
