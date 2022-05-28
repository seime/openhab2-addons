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
import org.openhab.binding.panasoniccomfortcloud.internal.model.OperationMode;
import org.openhab.binding.panasoniccomfortcloud.internal.model.Parameters;
import org.openhab.binding.panasoniccomfortcloud.internal.model.TemperatureRange;
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

    private final Logger logger = LoggerFactory.getLogger(PanasonicComfortCloudAirconditionHandler.class);
    private AirConditionerConfiguration config;

    public PanasonicComfortCloudAirconditionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(AirConditionerConfiguration.class);
        if (config != null) {
            logger.debug("Initializing air conditioner using config {}", config);
            super.initialize(config.deviceId);
            accountHandler.getModel().findDeviceByDeviceId(config.deviceId).ifPresent(device -> {
                loadFromServer();
            });
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
                Parameters currentParameters = device.getCurrentParameters();
                currentParameters.setFanAutoMode(airSwingAutoMode);
                sendParameters(channelUID, device, currentParameters,
                        StringType.valueOf(device.getCurrentParameters().getFanAutoMode().toString()));
            } catch (IllegalArgumentException e) {
                logger.debug("The device {} does not support unknown value {} for channel {}, valid values are {}",
                        device.getDeviceId(), command, channelUID, device.getFeatureSet().getSupportedEcoModes());
            }
        }
    }

    private void handleFanLevelCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getFanSpeed().toString()));
        } else {
            try {
                FanSpeed fanSpeed = FanSpeed.valueOf(command.toString());
                Parameters currentParameters = device.getCurrentParameters();
                currentParameters.setFanSpeed(fanSpeed);
                sendParameters(channelUID, device, currentParameters,
                        StringType.valueOf(device.getCurrentParameters().getFanSpeed().toString()));
            } catch (IllegalArgumentException e) {
                logger.debug("The device {} does not support unknown value {} for channel {}, valid values are {}",
                        device.getDeviceId(), command, channelUID, device.getFeatureSet().getSupportedEcoModes());
            }
        }
    }

    private void handleNanoeCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getNanoeMode().toString()));
        } else {
            logger.debug("Unsupported command {} for channel {}", command, channelUID);
        }
    }

    private void handleActualNanoeCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getActualNanoeMode().toString()));
        } else {
            logger.debug("Unsupported command {} for channel {}", command, channelUID);
        }
    }

    private void handleEcoModeCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(device.getCurrentParameters().getEcoMode().toString()));
        } else {
            try {
                EcoMode ecoMode = EcoMode.valueOf(command.toString());
                if (device.getFeatureSet().getSupportedEcoModes().contains(ecoMode)) {
                    Parameters currentParameters = device.getCurrentParameters();
                    currentParameters.setEcoMode(ecoMode);

                    sendParameters(channelUID, device, currentParameters,
                            StringType.valueOf(device.getCurrentParameters().getEcoMode().toString()));
                } else {
                    logger.debug(
                            "The device {} does not support setting value {} for channel {} - feature not supported in this AC",
                            device.getDeviceId(), command, channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug("The device {} does not support unknown value {} for channel {}, valid values are {}",
                        device.getDeviceId(), command, channelUID, device.getFeatureSet().getSupportedEcoModes());
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
                    Parameters currentParameters = device.getCurrentParameters();
                    currentParameters.setSwingSideways(airSwingSideways);

                    sendParameters(channelUID, device, currentParameters,
                            StringType.valueOf(device.getCurrentParameters().getSwingSideways().toString()));
                } else {
                    logger.debug(
                            "The device {} does not support setting value {} for channel {} - feature not supported in this AC",
                            device.getDeviceId(), command, channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug("The device {} does not support unknown value {} for channel {}, valid values are {}",
                        device.getDeviceId(), command, channelUID,
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
                    Parameters currentParameters = device.getCurrentParameters();
                    currentParameters.setSwingUpDown(airSwingUpDown);

                    sendParameters(channelUID, device, currentParameters,
                            StringType.valueOf(device.getCurrentParameters().getSwingUpDown().toString()));
                } else {
                    logger.debug(
                            "The device {} does not support setting value {} for channel {} - feature not supported in this AC",
                            device.getDeviceId(), command, channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug("The device {} does not support unknown value {} for channel {}, valid values are {}",
                        device.getDeviceId(), command, channelUID, device.getFeatureSet().getSupportedSwingUpDownModes()

                );
            }
        }
    }

    private void handleTargetTemperatureCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, new QuantityType<Temperature>(device.getCurrentParameters().getTargetTemperature(),
                    device.getTemperatureUnit()));
        } else {
            double targetTemperature = -1;
            if (command instanceof QuantityType) {
                targetTemperature = ((QuantityType) command).doubleValue();
            } else if (command instanceof DecimalType) {
                targetTemperature = ((DecimalType) command).doubleValue();
            }

            OperationMode mode = device.getCurrentParameters().getMode();
            if (OperationMode.AUTO == mode || OperationMode.COOL == mode || OperationMode.HEAT == mode) {
                TemperatureRange validRange = null;
                switch (mode) {
                    case AUTO:
                        validRange = device.getAutoRange();
                        break;
                    case COOL:
                        validRange = device.getCoolRange();
                        break;
                    case HEAT:
                        validRange = device.getHeatRange();
                        break;
                }
                if (validRange.isValid(targetTemperature)) {
                    Parameters currentParameters = device.getCurrentParameters();
                    currentParameters.setTargetTemperature(targetTemperature);

                    sendParameters(channelUID, device, currentParameters,
                            new QuantityType<Temperature>(targetTemperature, device.getTemperatureUnit()));

                } else {
                    logger.debug(
                            "The device {} does not support setting target temperature for channel {} to {}. Valid range {}",
                            device.getDeviceId(), channelUID, targetTemperature, validRange);
                }

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
                    Parameters currentParameters = device.getCurrentParameters();
                    currentParameters.setMode(operationMode);

                    sendParameters(channelUID, device, currentParameters,
                            StringType.valueOf(device.getCurrentParameters().getMode().toString()));
                } else {
                    logger.debug(
                            "The device {} does not support setting value {} for channel {} - feature not supported in this AC",
                            device.getDeviceId(), command, channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug("The device {} does not support unknown value {} for channel {}, valid values are {}",
                        device.getDeviceId(), command, channelUID, device.getFeatureSet().getSupportedOperationModes());
            }
        }
    }

    private void handleMasterSwitchCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            updateState(channelUID, OnOffType.from(device.getCurrentParameters().isMasterSwitch()));
        } else {
            if (command instanceof OnOffType) {
                Parameters currentParameters = device.getCurrentParameters();
                currentParameters.setMasterSwitch(command == OnOffType.ON ? true : false);
                sendParameters(channelUID, device, currentParameters, (OnOffType) command);
            } else {
                logger.debug("Unsupported command {} for channel {}", command, channelUID);
            }
        }
    }

    private void handleCurrentIndoorTemperatureCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {
            if (device.getCurrentParameters().getInsideTemperature() == null) {
                updateState(channelUID, UnDefType.UNDEF);
            } else {
                updateState(channelUID, new QuantityType<Temperature>(
                        device.getCurrentParameters().getInsideTemperature(), device.getTemperatureUnit()));
            }
        } else {
            logger.debug("Unsupported command {} for channel {}", command, channelUID);
        }
    }

    private void handleCurrentOutdoorTemperatureCommand(ChannelUID channelUID, Command command, Device device) {
        if (command instanceof RefreshType) {

            if (device.getCurrentParameters().getOutsideTemperature() == null) {
                updateState(channelUID, UnDefType.UNDEF);
            } else {
                updateState(channelUID, new QuantityType<Temperature>(
                        device.getCurrentParameters().getOutsideTemperature(), device.getTemperatureUnit()));
            }
        } else {
            logger.debug("Unsupported command {} for channel {}", command, channelUID);
        }
    }

    private void sendParameters(ChannelUID channelUID, Device device, Parameters currentParameters,
            State newStateIfSuccessfulUpdate) {
        try {
            SetDevicePropertiesResponse rsp = accountHandler.getApiBridge().sendRequest(
                    new SetDevicePropertiesRequest(device.getDeviceId(), currentParameters.toParametersDTO(device)),
                    SetDevicePropertiesResponse.class);
            if (rsp.code == 0) {
                updateState(channelUID, newStateIfSuccessfulUpdate);
            } else {
                logger.info("Error sending parameters to device {} for channel {}", device.getDeviceId(), channelUID);
            }
        } catch (PanasonicComfortCloudException e) {
            logger.debug("Error updating AC parameter", e);
        }
    }
}
