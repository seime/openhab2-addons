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
package org.openhab.binding.bluetooth.secuyou.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * This discovery participant is able to recognize Smart Lock devices and create discovery results for them.
 *
 * @author Arne Seime - Initial contribution
 * 
 */
@NonNullByDefault
@Component
public class SecuyouDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    private static final int SECUYOU_COMPANY_ID = 724;

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SecuyouBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        if (isSecuyouDevice(device)) {
            return new ThingUID(SecuyouBindingConstants.THING_TYPE_SMART_LOCK, device.getAdapter().getUID(),
                    device.getAddress().toString().toLowerCase().replace(":", ""));

        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        if (!isSecuyouDevice(device)) {
            return null;
        }
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }
        return createResult(device, thingUID);
    }

    @Override
    public boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return isSecuyouDevice(device);
    }

    private boolean isSecuyouDevice(BluetoothDiscoveryDevice device) {
        Integer manufacturerId = device.getManufacturerId();
        return manufacturerId != null && manufacturerId == SECUYOU_COMPANY_ID;
    }

    private DiscoveryResult createResult(BluetoothDiscoveryDevice device, ThingUID thingUID) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
        properties.put(Thing.PROPERTY_VENDOR, "Secuyou Aps");
        properties.put(Thing.PROPERTY_MAC_ADDRESS, device.getAddress().toString());

        String label = device.getName() + " (Secuyou Smart Lock)";

        // Create the discovery result and add to the inbox
        return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS)
                .withBridge(device.getAdapter().getUID()).withLabel(label).build();
    }
}
