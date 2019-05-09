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
package org.openhab.binding.sensibo.internal.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arne Seime - Initial contribution
 */
public abstract class SensiboBaseThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SensiboBaseThingHandler.class);

    public SensiboBaseThingHandler(final Thing thing) {
        super(thing);
    }

    public void updateState(final SensiboModel model) {
        for (final Channel channel : getThing().getChannels()) {
            handleCommand(channel.getUID(), RefreshType.REFRESH, model);
        }
    }

    protected SensiboModel getSensiboModel() {
        final SensiboAccountHandler accountHandler = getAccountHandler();
        if (accountHandler != null) {
            return accountHandler.getModel();
        }
        logger.warn(
                "Thing {} cannot exist without a bridge and account handler - returning empty model. No heaters or rooms will be found",
                getThing().getUID());
        return new SensiboModel(0);
    }

    protected SensiboAccountHandler getAccountHandler() {
        final Bridge bridge = getBridge();
        if (bridge != null) {
            final SensiboAccountHandler accountHandler = (SensiboAccountHandler) bridge.getHandler();
            if (accountHandler != null) {
                return accountHandler;
            }
        }
        return null;
    }

    protected abstract void handleCommand(@NonNull ChannelUID uid, @NonNull Command command,
            @NonNull SensiboModel model);

}
