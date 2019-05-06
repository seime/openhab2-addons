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
package org.openhab.binding.sensibo.internal;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.sensibo.internal.handler.SensiboSkyHandler;

/**
 * Provide a custom channel type for available inputs
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Refactoring the input source names.
 */
@NonNullByDefault
public class CallbackChannelsTypeProvider implements ChannelTypeProvider, ThingHandlerService {
    private @NonNullByDefault({}) SensiboSkyHandler handler;

    @Override
    public @Nullable Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return handler.getChannelTypes(locale);
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return handler.getChannelType(channelTypeUID, locale);
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        return null;
    }

    @Override
    public @Nullable Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return null;
    }

    @NonNullByDefault({})
    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (SensiboSkyHandler) handler;
    }

    @Override
    public ThingHandler getThingHandler() {
        return handler;
    }
}
