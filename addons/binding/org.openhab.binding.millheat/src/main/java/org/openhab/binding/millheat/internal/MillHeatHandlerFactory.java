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
package org.openhab.binding.millheat.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.millheat.internal.handler.MillHeatBridgeHandler;
import org.openhab.binding.millheat.internal.handler.MillHeatHeaterHandler;
import org.openhab.binding.millheat.internal.handler.MillHeatRoomHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MillHeatHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.millheat", service = ThingHandlerFactory.class)
public class MillHeatHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(MillHeatBindingConstants.THING_TYPE_BRIDGE, MillHeatBindingConstants.THING_TYPE_HEATER,
                    MillHeatBindingConstants.THING_TYPE_ROOM)
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(MillHeatHandlerFactory.class);

    @Nullable
    private HttpClient httpClient = null;

    private Map<ThingUID, ServiceRegistration<DiscoveryService>> discoveryServiceRegistrations = new HashMap<>();

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (MillHeatBindingConstants.THING_TYPE_HEATER.equals(thingTypeUID)) {
            return new MillHeatHeaterHandler(thing);
        } else if (MillHeatBindingConstants.THING_TYPE_ROOM.equals(thingTypeUID)) {
            return new MillHeatRoomHandler(thing);
        } else if (MillHeatBindingConstants.THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            if (httpClient != null) {
                MillHeatBridgeHandler handler = new MillHeatBridgeHandler((Bridge) thing, httpClient);
                MillHeatDiscoveryService service = new MillHeatDiscoveryService(handler);
                ServiceRegistration<DiscoveryService> serviceRegistration = this.bundleContext
                        .registerService(DiscoveryService.class, service, null);

                handler.setDiscoveryService(service);
                discoveryServiceRegistrations.put(handler.getThing().getUID(), serviceRegistration);

                return handler;

            } else {
                logger.error("HttpClient is null, cannot instantiate bridge");
            }
        }

        return null;
    }

    @Override
    protected void removeHandler(@NonNull ThingHandler thingHandler) {
        ServiceRegistration<DiscoveryService> serviceRegistration = discoveryServiceRegistrations
                .get(thingHandler.getThing().getUID());
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

}
