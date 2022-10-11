/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.panasoniccomfortcloud.internal.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants;
import org.openhab.binding.panasoniccomfortcloud.internal.discovery.PanasonicComfortCloudDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PanasonicComfortCloudHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.panasoniccomfortcloud", service = ThingHandlerFactory.class)
public class PanasonicComfortCloudHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(BindingConstants.THING_TYPE_ACCOUNT, BindingConstants.THING_TYPE_AIRCONDITION)
                    .collect(Collectors.toSet()));
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private StorageService storageService;

    @Activate
    public PanasonicComfortCloudHandlerFactory(@Reference StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (BindingConstants.THING_TYPE_AIRCONDITION.equals(thingTypeUID)) {
            return new PanasonicComfortCloudAirconditionHandler(thing);
        } else if (BindingConstants.THING_TYPE_WATERHEATPUMP.equals(thingTypeUID)) {
            return new PanasonicComfortCloudWaterHeatpumpHandler(thing);
        } else if (BindingConstants.THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            PanasonicComfortCloudAccountHandler handler = new PanasonicComfortCloudAccountHandler((Bridge) thing,
                    storageService);
            registerDeviceDiscoveryService(handler);
            return handler;
        }
        return null;
    }

    private void registerDeviceDiscoveryService(PanasonicComfortCloudAccountHandler bridgeHandler) {
        PanasonicComfortCloudDiscoveryService discoveryService = new PanasonicComfortCloudDiscoveryService(
                bridgeHandler);
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private void unregisterDeviceDiscoveryService(ThingUID thingUID) {
        if (discoveryServiceRegs.containsKey(thingUID)) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegs.get(thingUID);
            serviceReg.unregister();
            discoveryServiceRegs.remove(thingUID);
        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof PanasonicComfortCloudAccountHandler) {
            ThingUID thingUID = thingHandler.getThing().getUID();
            unregisterDeviceDiscoveryService(thingUID);
        }
        super.removeHandler(thingHandler);
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }
}
