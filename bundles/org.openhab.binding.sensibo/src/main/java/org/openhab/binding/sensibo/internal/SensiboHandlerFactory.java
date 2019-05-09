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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.sensibo.internal.handler.SensiboAccountHandler;
import org.openhab.binding.sensibo.internal.handler.SensiboSkyHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SensiboHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.sensibo", service = ThingHandlerFactory.class)
public class SensiboHandlerFactory extends BaseThingHandlerFactory {
    private @NonNullByDefault({}) HttpClient httpClient;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(SensiboBindingConstants.THING_TYPE_ACCOUNT, SensiboBindingConstants.THING_TYPE_SENSIBOSKY)
                    .collect(Collectors.toSet()));

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (SensiboBindingConstants.THING_TYPE_SENSIBOSKY.equals(thingTypeUID)) {
            return new SensiboSkyHandler(thing);
        } else if (SensiboBindingConstants.THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            final SensiboAccountHandler handler = new SensiboAccountHandler((Bridge) thing, httpClient, bundleContext);
            return handler;
        }
        return null;
    }

    @Reference
    protected void setHttpClientFactory(final HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(final HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }
}
