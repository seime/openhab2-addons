package org.openhab.binding.panasoniccomfortcloud.internal.handler;

import java.util.Optional;

import org.openhab.binding.panasoniccomfortcloud.internal.config.AirConditionerConfiguration;
import org.openhab.binding.panasoniccomfortcloud.internal.model.Device;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.AirconditionDevice;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PanasonicComfortCloudWaterHeatpumpHandler extends PanasonicComfortCloudBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PanasonicComfortCloudWaterHeatpumpHandler.class);

    protected PanasonicComfortCloudWaterHeatpumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void loadFromServer() {
        // super.loadFromServer();
    }

    @Override
    protected void handleCommand(ChannelUID channelUID, Command command, AirconditionDevice airconditionDevice) {
    }

    @Override
    public void initialize() {
        AirConditionerConfiguration config = getConfigAs(AirConditionerConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("Initializing water heat pimp using config {}", config);
        super.initialize(config.deviceId);
        Optional<Device> device = accountHandler.getModel().findDeviceByDeviceId(config.deviceId);
        if (device.isPresent()) {
            loadFromServer();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not find device in internal model, check deviceId configuration");
        }
    }
}
