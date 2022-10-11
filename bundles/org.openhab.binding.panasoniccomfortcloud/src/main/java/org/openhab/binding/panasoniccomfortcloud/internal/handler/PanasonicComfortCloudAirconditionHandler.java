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
import org.openhab.binding.panasoniccomfortcloud.internal.model.Device;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.AirSwingAutoMode;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.AirSwingSideways;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.AirSwingUpDown;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.AirconditionDevice;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.EcoMode;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.FanSpeed;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.NanoeMode;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.OperationMode;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.Parameters;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.TemperatureRange;
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
    protected synchronized void handleCommand(final ChannelUID channelUID, final Command command,
            final AirconditionDevice airconditionDevice) {
        if (airconditionDevice.isInitialized()) {
            switch (channelUID.getId()) {
                case CHANNEL_CURRENT_INDOOR_TEMPERATURE:
                    handleCurrentIndoorTemperatureCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_CURRENT_OUTDOOR_TEMPERATURE:
                    handleCurrentOutdoorTemperatureCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_MASTER_SWITCH:
                    handleMasterSwitchCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_OPERATION_MODE:
                    handleOperatingModeCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_ECO_MODE:
                    handleEcoModeCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_FAN_SPEED:
                    handleFanLevelCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_AIR_SWING_AUTO_MODE:
                    handleFanAutoModeCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_TARGET_TEMPERATURE:
                    handleTargetTemperatureCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_AIR_SWING_HORIZONTAL:
                    handleHorizontalSwingCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_AIR_SWING_VERTICAL:
                    handleVerticalSwingCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_NANOE:
                    handleNanoeCommand(channelUID, command, airconditionDevice);
                    break;
                case CHANNEL_ACTUAL_NANOE:
                    handleActualNanoeCommand(channelUID, command, airconditionDevice);
                    break;
                default:
                    logger.debug("Received command on unknown channel {}, ignoring", channelUID.getId());
            }
        } else {
            logger.debug(
                    "Received command {} for airconditionDevice {} on channel {}, but devices is not yet initialized",
                    command, airconditionDevice.getDeviceId(), channelUID);
            updateState(channelUID, UnDefType.UNDEF);
        }
    }

    private void handleFanAutoModeCommand(ChannelUID channelUID, Command command,
            AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID,
                    StringType.valueOf(airconditionDevice.getCurrentParameters().getFanAutoMode().toString()));
        } else {

            try {
                AirSwingAutoMode airSwingAutoMode = AirSwingAutoMode.valueOf(command.toString());
                Parameters currentParameters = airconditionDevice.getCurrentParameters();
                currentParameters.setFanAutoMode(airSwingAutoMode);
                sendParameters(channelUID, airconditionDevice, currentParameters,
                        StringType.valueOf(airconditionDevice.getCurrentParameters().getFanAutoMode().toString()));
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, airconditionDevice.getDeviceId(), command, channelUID,
                        airconditionDevice.getFeatureSet().getSupportedEcoModes());
            }
        }
    }

    private void handleFanLevelCommand(ChannelUID channelUID, Command command, AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID,
                    StringType.valueOf(airconditionDevice.getCurrentParameters().getFanSpeed().toString()));
        } else {
            try {
                FanSpeed fanSpeed = FanSpeed.valueOf(command.toString());
                Parameters currentParameters = airconditionDevice.getCurrentParameters();
                currentParameters.setFanSpeed(fanSpeed);
                sendParameters(channelUID, airconditionDevice, currentParameters,
                        StringType.valueOf(airconditionDevice.getCurrentParameters().getFanSpeed().toString()));
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, airconditionDevice.getDeviceId(), command, channelUID,
                        airconditionDevice.getFeatureSet().getSupportedEcoModes());
            }
        }
    }

    private void handleNanoeCommand(ChannelUID channelUID, Command command, AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID,
                    StringType.valueOf(airconditionDevice.getCurrentParameters().getNanoeMode().toString()));
        } else if (airconditionDevice.getFeatureSet().isNanoeStandAlone()) {
            NanoeMode nanoeMode = NanoeMode.valueOf(command.toString());
            Parameters currentParameters = airconditionDevice.getCurrentParameters();
            currentParameters.setNanoeMode(nanoeMode);
            sendParameters(channelUID, airconditionDevice, currentParameters,
                    StringType.valueOf(airconditionDevice.getCurrentParameters().getNanoeMode().toString()));
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void handleActualNanoeCommand(ChannelUID channelUID, Command command,
            AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID,
                    StringType.valueOf(airconditionDevice.getCurrentParameters().getActualNanoeMode().toString()));
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void handleEcoModeCommand(ChannelUID channelUID, Command command, AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID,
                    StringType.valueOf(airconditionDevice.getCurrentParameters().getEcoMode().toString()));
        } else {
            try {
                EcoMode ecoMode = EcoMode.valueOf(command.toString());
                if (airconditionDevice.getFeatureSet().getSupportedEcoModes().contains(ecoMode)) {
                    Parameters currentParameters = airconditionDevice.getCurrentParameters();
                    currentParameters.setEcoMode(ecoMode);

                    sendParameters(channelUID, airconditionDevice, currentParameters,
                            StringType.valueOf(airconditionDevice.getCurrentParameters().getEcoMode().toString()));
                } else {
                    logger.debug(ERROR_MESSAGE_UNSUPPORTED_FEATURE, airconditionDevice.getDeviceId(), command,
                            channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, airconditionDevice.getDeviceId(), command, channelUID,
                        airconditionDevice.getFeatureSet().getSupportedEcoModes());
            }
        }
    }

    private void handleHorizontalSwingCommand(ChannelUID channelUID, Command command,
            AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID,
                    StringType.valueOf(airconditionDevice.getCurrentParameters().getSwingSideways().toString()));
        } else {
            try {
                AirSwingSideways airSwingSideways = AirSwingSideways.valueOf(command.toString());
                if (airconditionDevice.getFeatureSet().getSupportedSwingSidewayModes().contains(airSwingSideways)) {
                    Parameters currentParameters = airconditionDevice.getCurrentParameters();
                    currentParameters.setSwingSideways(airSwingSideways);

                    sendParameters(channelUID, airconditionDevice, currentParameters, StringType
                            .valueOf(airconditionDevice.getCurrentParameters().getSwingSideways().toString()));
                } else {
                    logger.debug(ERROR_MESSAGE_UNSUPPORTED_FEATURE, airconditionDevice.getDeviceId(), command,
                            channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, airconditionDevice.getDeviceId(), command, channelUID,
                        airconditionDevice.getFeatureSet().getSupportedSwingSidewayModes());
            }
        }
    }

    private void handleVerticalSwingCommand(ChannelUID channelUID, Command command,
            AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID,
                    StringType.valueOf(airconditionDevice.getCurrentParameters().getSwingUpDown().toString()));
        } else {
            try {
                AirSwingUpDown airSwingUpDown = AirSwingUpDown.valueOf(command.toString());
                if (airconditionDevice.getFeatureSet().getSupportedSwingUpDownModes().contains(airSwingUpDown)) {
                    Parameters currentParameters = airconditionDevice.getCurrentParameters();
                    currentParameters.setSwingUpDown(airSwingUpDown);

                    sendParameters(channelUID, airconditionDevice, currentParameters,
                            StringType.valueOf(airconditionDevice.getCurrentParameters().getSwingUpDown().toString()));
                } else {
                    logger.debug(ERROR_MESSAGE_UNSUPPORTED_FEATURE, airconditionDevice.getDeviceId(), command,
                            channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, airconditionDevice.getDeviceId(), command, channelUID,
                        airconditionDevice.getFeatureSet().getSupportedSwingUpDownModes()

                );
            }
        }
    }

    private void handleTargetTemperatureCommand(ChannelUID channelUID, Command command,
            AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID, new QuantityType<>(airconditionDevice.getCurrentParameters().getTargetTemperature(),
                    airconditionDevice.getTemperatureUnit()));
        } else {
            double targetTemperature = -1;
            if (command instanceof QuantityType) {
                targetTemperature = ((QuantityType<Temperature>) command).doubleValue();
            } else if (command instanceof DecimalType) {
                targetTemperature = ((DecimalType) command).doubleValue();
            }

            OperationMode mode = airconditionDevice.getCurrentParameters().getMode();
            if (OperationMode.AUTO == mode || OperationMode.COOL == mode || OperationMode.HEAT == mode) {
                TemperatureRange validRange = null;
                switch (mode) {
                    case AUTO:
                        validRange = airconditionDevice.getAutoRange();
                        break;
                    case COOL:
                        validRange = airconditionDevice.getCoolRange();
                        break;
                    case HEAT:
                        validRange = airconditionDevice.getHeatRange();
                        break;
                    default:
                        logger.warn("Found no valid temperature range for mode {}", mode);
                }
                if (validRange != null && validRange.isValid(targetTemperature)) {
                    Parameters currentParameters = airconditionDevice.getCurrentParameters();
                    currentParameters.setTargetTemperature(targetTemperature);

                    sendParameters(channelUID, airconditionDevice, currentParameters,
                            new QuantityType<Temperature>(targetTemperature, airconditionDevice.getTemperatureUnit()));

                } else {
                    logger.debug(
                            "The airconditionDevice {} does not support setting target temperature for channel {} to {}. Valid range {}",
                            airconditionDevice.getDeviceId(), channelUID, targetTemperature, validRange);
                }

            } else {
                logger.debug(
                        "The airconditionDevice {} does not support setting target temperature for channel {} - in mode {}. Change mode to AUTO, COOL or HEAT to set target temperature",
                        airconditionDevice.getDeviceId(), channelUID, mode);

            }

        }
    }

    private void handleOperatingModeCommand(ChannelUID channelUID, Command command,
            AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(airconditionDevice.getCurrentParameters().getMode().toString()));
        } else {
            try {
                OperationMode operationMode = OperationMode.valueOf(command.toString());
                if (airconditionDevice.getFeatureSet().getSupportedOperationModes().contains(operationMode)) {
                    Parameters currentParameters = airconditionDevice.getCurrentParameters();
                    currentParameters.setMode(operationMode);

                    sendParameters(channelUID, airconditionDevice, currentParameters,
                            StringType.valueOf(airconditionDevice.getCurrentParameters().getMode().toString()));
                } else {
                    logger.debug(ERROR_MESSAGE_UNSUPPORTED_FEATURE, airconditionDevice.getDeviceId(), command,
                            channelUID);
                }
            } catch (IllegalArgumentException e) {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_VALUE, airconditionDevice.getDeviceId(), command, channelUID,
                        airconditionDevice.getFeatureSet().getSupportedOperationModes());
            }
        }
    }

    private void handleMasterSwitchCommand(ChannelUID channelUID, Command command,
            AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            updateState(channelUID, OnOffType.from(airconditionDevice.getCurrentParameters().isMasterSwitch()));
        } else {
            if (command instanceof OnOffType) {
                Parameters currentParameters = airconditionDevice.getCurrentParameters();
                currentParameters.setMasterSwitch(command == OnOffType.ON);
                sendParameters(channelUID, airconditionDevice, currentParameters, (OnOffType) command);
            } else {
                logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
            }
        }
    }

    private void handleCurrentIndoorTemperatureCommand(ChannelUID channelUID, Command command,
            AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {
            if (airconditionDevice.getCurrentParameters().getInsideTemperature() == null) {
                updateState(channelUID, UnDefType.UNDEF);
            } else {
                updateState(channelUID,
                        new QuantityType<>(airconditionDevice.getCurrentParameters().getInsideTemperature(),
                                airconditionDevice.getTemperatureUnit()));
            }
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void handleCurrentOutdoorTemperatureCommand(ChannelUID channelUID, Command command,
            AirconditionDevice airconditionDevice) {
        if (command instanceof RefreshType) {

            if (airconditionDevice.getCurrentParameters().getOutsideTemperature() == null) {
                updateState(channelUID, UnDefType.UNDEF);
            } else {
                updateState(channelUID,
                        new QuantityType<>(airconditionDevice.getCurrentParameters().getOutsideTemperature(),
                                airconditionDevice.getTemperatureUnit()));
            }
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void sendParameters(ChannelUID channelUID, AirconditionDevice airconditionDevice,
            Parameters currentParameters, State newStateIfSuccessfulUpdate) {
        try {
            SetDevicePropertiesResponse rsp = accountHandler.getApiBridge()
                    .sendRequest(
                            new SetDevicePropertiesRequest(airconditionDevice.getDeviceId(),
                                    currentParameters.toParametersDTO(airconditionDevice)),
                            SetDevicePropertiesResponse.class);
            if (rsp.code == 0) {
                updateState(channelUID, newStateIfSuccessfulUpdate);
            } else {
                logger.info("Error sending parameters to airconditionDevice {} for channel {}",
                        airconditionDevice.getDeviceId(), channelUID);
            }
        } catch (PanasonicComfortCloudException e) {
            logger.debug("Error updating AC parameter", e);
        }
    }
}
