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

import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_ACTUAL_NANOE;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_AIR_SWING_AUTO_MODE;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_AIR_SWING_HORIZONTAL;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_AIR_SWING_VERTICAL;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_CURRENT_INDOOR_TEMPERATURE;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_CURRENT_OUTDOOR_TEMPERATURE;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_ECO_MODE;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_FAN_SPEED;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_MASTER_SWITCH;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_NANOE;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_OPERATION_MODE;
import static org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants.CHANNEL_TARGET_TEMPERATURE;

import java.util.Optional;

import javax.measure.quantity.Temperature;

import org.openhab.binding.panasoniccomfortcloud.internal.PanasonicComfortCloudException;
import org.openhab.binding.panasoniccomfortcloud.internal.config.AirConditionerConfiguration;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.SetDevicePropertiesRequest;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.SetDevicePropertiesResponse;
import org.openhab.binding.panasoniccomfortcloud.internal.model.AirSwingAutoMode;
import org.openhab.binding.panasoniccomfortcloud.internal.model.AirSwingSideways;
import org.openhab.binding.panasoniccomfortcloud.internal.model.AirSwingUpDown;
import org.openhab.binding.panasoniccomfortcloud.internal.model.Device;
import org.openhab.binding.panasoniccomfortcloud.internal.model.EcoMode;
import org.openhab.binding.panasoniccomfortcloud.internal.model.FanSpeed;
import org.openhab.binding.panasoniccomfortcloud.internal.model.NanoeMode;
import org.openhab.binding.panasoniccomfortcloud.internal.model.OperationMode;
import org.openhab.binding.panasoniccomfortcloud.internal.model.Parameters;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicComfortCloudAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
public class PanasonicComfortCloudAirconditionHandler extends PanasonicComfortCloudBaseThingHandler {

    public static final String ERROR_MESSAGE_UNSUPPORTED_VALUE = "The device {} does not support unknown value {} for channel {}, valid values are {}";
    public static final String ERROR_MESSAGE_UNSUPPORTED_COMMAND = "Unsupported command {} for channel {}";
    public static final String ERROR_MESSAGE_UNSUPPORTED_FEATURE = "The device {} does not support setting value {} for channel {} - feature not supported in this AC";
    private final Logger logger = LoggerFactory.getLogger(PanasonicComfortCloudAirconditionHandler.class);

    public PanasonicComfortCloudAirconditionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        AirConditionerConfiguration config = getConfigAs(AirConditionerConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("Initializing air conditioner using config {}", config);
        super.initialize(config.deviceId);
        Optional<Device> device = accountHandler.getModel().findDeviceByDeviceId(config.deviceId);
        if (device.isPresent()) {
            loadFromServer();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not find device in internal model, check deviceId configuration");
        }
    }

    @Override
    protected synchronized void handleCommand(final ChannelUID channelUID, final Command command, final Device device) {
        if (device.isInitialized()) {
            switch (channelUID.getId()) {
                case CHANNEL_CURRENT_INDOOR_TEMPERATURE:
                    handleCurrentIndoorTemperatureCommand(channelUID, command, device);
                    break;
                case CHANNEL_CURRENT_OUTDOOR_TEMPERATURE:
                    handleCurrentOutdoorTemperatureCommand(channelUID, command, device);
                    break;
                case CHANNEL_MASTER_SWITCH:
                    handleMasterSwitchCommand(channelUID, command, device);
                    break;
                case CHANNEL_OPERATION_MODE:
                    handleOperatingModeCommand(channelUID, command, device);
                    break;
                case CHANNEL_ECO_MODE:
                    handleEcoModeCommand(channelUID, command, device);
                    break;
                case CHANNEL_FAN_SPEED:
                    handleFanLevelCommand(channelUID, command, device);
                    break;
                case CHANNEL_AIR_SWING_AUTO_MODE:
                    handleFanAutoModeCommand(channelUID, command, device);
                    break;
                case CHANNEL_TARGET_TEMPERATURE:
                    handleTargetTemperatureCommand(channelUID, command, device);
                    break;
                case CHANNEL_AIR_SWING_HORIZONTAL:
                    handleHorizontalSwingCommand(channelUID, command, device);
                    break;
                case CHANNEL_AIR_SWING_VERTICAL:
                    handleVerticalSwingCommand(channelUID, command, device);
                    break;
                case CHANNEL_NANOE:
                    handleNanoeCommand(channelUID, command, device);
                    break;
                case CHANNEL_ACTUAL_NANOE:
                    handleActualNanoeCommand(channelUID, command, device);
                    break;
                default:
                    logger.debug("Received command on unknown channel {}, ignoring", channelUID.getId());
            }
        } else {
            logger.debug("Received command {} for device {} on channel {}, but devices is not yet initialized", command,
                    device.getDeviceId(), channelUID);
            updateState(channelUID, UnDefType.UNDEF);
        }
    }

    private void handleFanAutoModeCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getFanAutoMode().toString()));
        } else {

            try {
                AirSwingAutoMode airSwingAutoMode = AirSwingAutoMode.valueOf(command.toString());
                Parameters newParameters = device.createSendRequestParameters();
                newParameters.setFanAutoMode(airSwingAutoMode);
                sendParameters(channelUID, device, newParameters,
                        StringType.valueOf(newParameters.getFanAutoMode().toString()));
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, device.getDeviceId(), command, channelUID,
                        device.getFeatureSet().getSupportedEcoModes());
            }
        }
    }

    private void handleFanLevelCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getFanSpeed().toString()));
        } else {
            try {
                FanSpeed fanSpeed = FanSpeed.valueOf(command.toString());
                Parameters newParameters = device.createSendRequestParameters();
                newParameters.setFanSpeed(fanSpeed);

                sendParameters(channelUID, device, newParameters,
                        StringType.valueOf(newParameters.getFanSpeed().toString()));
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, device.getDeviceId(), command, channelUID,
                        device.getFeatureSet().getSupportedEcoModes());
            }
        }
    }

    private void handleNanoeCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getNanoeMode().toString()));
        } else if (device.getFeatureSet().isNanoeStandAlone()) {
            NanoeMode nanoeMode = NanoeMode.valueOf(command.toString());
            Parameters newParameters = device.createSendRequestParameters();
            newParameters.setNanoeMode(nanoeMode);
            sendParameters(channelUID, device, newParameters,
                    StringType.valueOf(newParameters.getNanoeMode().toString()));
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void handleActualNanoeCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getActualNanoeMode().toString()));
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void handleEcoModeCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getEcoMode().toString()));
        } else {
            try {
                EcoMode ecoMode = EcoMode.valueOf(command.toString());
                if (device.getFeatureSet().getSupportedEcoModes().contains(ecoMode)) {
                    Parameters newParameters = device.createSendRequestParameters();
                    newParameters.setEcoMode(ecoMode);

                    sendParameters(channelUID, device, newParameters,
                            StringType.valueOf(newParameters.getEcoMode().toString()));
                } else {
                    logger.debug(ERROR_MESSAGE_UNSUPPORTED_FEATURE, device.getDeviceId(), command, channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, device.getDeviceId(), command, channelUID,
                        device.getFeatureSet().getSupportedEcoModes());
            }
        }
    }

    private void handleHorizontalSwingCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getSwingSideways().toString()));
        } else {
            try {
                AirSwingSideways airSwingSideways = AirSwingSideways.valueOf(command.toString());
                if (device.getFeatureSet().getSupportedSwingSidewayModes().contains(airSwingSideways)) {
                    Parameters newParameters = device.createSendRequestParameters();
                    newParameters.setSwingSideways(airSwingSideways);

                    sendParameters(channelUID, device, newParameters,
                            StringType.valueOf(newParameters.getSwingSideways().toString()));
                } else {
                    logger.debug(ERROR_MESSAGE_UNSUPPORTED_FEATURE, device.getDeviceId(), command, channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, device.getDeviceId(), command, channelUID,
                        device.getFeatureSet().getSupportedSwingSidewayModes());
            }
        }
    }

    private void handleVerticalSwingCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getSwingUpDown().toString()));
        } else {
            try {
                AirSwingUpDown airSwingUpDown = AirSwingUpDown.valueOf(command.toString());
                if (device.getFeatureSet().getSupportedSwingUpDownModes().contains(airSwingUpDown)) {
                    Parameters newParameters = device.createSendRequestParameters();
                    newParameters.setSwingUpDown(airSwingUpDown);

                    sendParameters(channelUID, device, newParameters,
                            StringType.valueOf(newParameters.getSwingUpDown().toString()));
                } else {
                    logger.debug(ERROR_MESSAGE_UNSUPPORTED_FEATURE, device.getDeviceId(), command, channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, device.getDeviceId(), command, channelUID,
                        device.getFeatureSet().getSupportedSwingUpDownModes()

                );
            }
        }
    }

    private void handleTargetTemperatureCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            if (device.getCurrentParameters().getTargetTemperature() == null) {
                updateState(channelUID, UnDefType.UNDEF);
            } else {
                updateState(channelUID, new QuantityType<>(device.getCurrentParameters().getTargetTemperature(),
                        device.getTemperatureUnit()));
            }
        } else {
            double targetTemperature = -1;
            if (command instanceof QuantityType) {
                targetTemperature = ((QuantityType<Temperature>) command).doubleValue();
            } else if (command instanceof DecimalType) {
                targetTemperature = ((DecimalType) command).doubleValue();
            }

            OperationMode mode = device.getCurrentParameters().getMode();
            if (OperationMode.AUTO == mode || OperationMode.COOL == mode || OperationMode.HEAT == mode) {
                Parameters newParameters = device.createSendRequestParameters();
                newParameters.setTargetTemperature(targetTemperature);

                sendParameters(channelUID, device, newParameters,
                        new QuantityType<>(targetTemperature, device.getTemperatureUnit()));
            } else {
                logger.debug(
                        "The device {} does not support setting target temperature for channel {} - in mode {}. Change mode to AUTO, COOL or HEAT to set target temperature",
                        device.getDeviceId(), channelUID, mode);

            }

        }
    }

    private void handleOperatingModeCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getMode().toString()));
        } else {
            try {
                OperationMode operationMode = OperationMode.valueOf(command.toString());
                if (device.getFeatureSet().getSupportedOperationModes().contains(operationMode)) {
                    Parameters newParameters = device.createSendRequestParameters();
                    newParameters.setMode(operationMode);

                    sendParameters(channelUID, device, newParameters,
                            StringType.valueOf(newParameters.getMode().toString()));
                } else {
                    logger.debug(ERROR_MESSAGE_UNSUPPORTED_FEATURE, device.getDeviceId(), command, channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, device.getDeviceId(), command, channelUID,
                        device.getFeatureSet().getSupportedOperationModes());
            }
        }
    }

    private void handleMasterSwitchCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, OnOffType.from(device.getCurrentParameters().isMasterSwitch()));
        } else {
            if (command instanceof OnOffType) {
                Parameters newParameters = device.createSendRequestParameters();
                newParameters.setMasterSwitch(command == OnOffType.ON);
                sendParameters(channelUID, device, newParameters, (OnOffType) command);
            } else {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
            }
        }
    }

    private void handleCurrentIndoorTemperatureCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            if (device.getCurrentParameters().getInsideTemperature() == null) {
                updateState(channelUID, UnDefType.UNDEF);
            } else {
                updateState(channelUID, new QuantityType<>(device.getCurrentParameters().getInsideTemperature(),
                        device.getTemperatureUnit()));
            }
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void handleCurrentOutdoorTemperatureCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {

            if (device.getCurrentParameters().getOutsideTemperature() == null) {
                updateState(channelUID, UnDefType.UNDEF);
            } else {
                updateState(channelUID, new QuantityType<>(device.getCurrentParameters().getOutsideTemperature(),
                        device.getTemperatureUnit()));
            }
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void sendParameters(ChannelUID channelUID, Device device, Parameters newParameters,
            State newStateIfSuccessfulUpdate) {
        try {
            SetDevicePropertiesResponse rsp = accountHandler.getApiBridge().sendRequest(
                    new SetDevicePropertiesRequest(device.getDeviceId(), newParameters.toParametersDTO(device)),
                    SetDevicePropertiesResponse.class);
            if (rsp.code == 0) {
                updateState(channelUID, newStateIfSuccessfulUpdate);

            } else {
                logger.warn(
                        "Error sending parameters to device {} for channel {}. Server responded with code {} and message {}",
                        device.getDeviceId(), channelUID, rsp.code, rsp.error);
            }
        } catch (PanasonicComfortCloudException e) {
            logger.warn("Error updating AC parameter {}", e.getMessage());
            logger.debug("Error updating AC parameter", e);
        }
    }
}
