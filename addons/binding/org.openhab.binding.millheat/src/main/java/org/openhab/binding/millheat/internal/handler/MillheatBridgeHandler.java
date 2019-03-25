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
package org.openhab.binding.millheat.internal.handler;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.millheat.internal.MillheatCommunicationException;
import org.openhab.binding.millheat.internal.client.BooleanSerializer;
import org.openhab.binding.millheat.internal.client.RequestLogger;
import org.openhab.binding.millheat.internal.config.MillheatBridgeConfiguration;
import org.openhab.binding.millheat.internal.discovery.MillheatDiscoveryService;
import org.openhab.binding.millheat.internal.dto.AbstractRequest;
import org.openhab.binding.millheat.internal.dto.AbstractResponse;
import org.openhab.binding.millheat.internal.dto.GetHomesRequest;
import org.openhab.binding.millheat.internal.dto.GetHomesResponse;
import org.openhab.binding.millheat.internal.dto.GetIndependentDevicesByHomeRequest;
import org.openhab.binding.millheat.internal.dto.GetIndependentDevicesByHomeResponse;
import org.openhab.binding.millheat.internal.dto.LoginRequest;
import org.openhab.binding.millheat.internal.dto.LoginResponse;
import org.openhab.binding.millheat.internal.dto.SelectDeviceByRoomRequest;
import org.openhab.binding.millheat.internal.dto.SelectDeviceByRoomResponse;
import org.openhab.binding.millheat.internal.dto.SelectRoomByHomeRequest;
import org.openhab.binding.millheat.internal.dto.SelectRoomByHomeResponse;
import org.openhab.binding.millheat.internal.dto.SetDeviceTempRequest;
import org.openhab.binding.millheat.internal.dto.SetRoomTempRequest;
import org.openhab.binding.millheat.internal.dto.SetRoomTempResponse;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.ModeType;
import org.openhab.binding.millheat.internal.model.Room;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link MillheatBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillheatBridgeHandler extends BaseBridgeHandler {

    private static final int MIN_TIME_BETWEEEN_MODEL_UPDATES_MS = 30_000;

    private static final int NUM_NONCE_CHARS = 16;

    private static final String CONTENT_TYPE = "application/x-zc-object";

    public static String API_ENDPOINT_1 = "https://eurouter.ablecloud.cn:9005/zc-account/v1/";

    public static String API_ENDPOINT_2 = "https://eurouter.ablecloud.cn:9005/millService/v1/";

    private static final String ALLOWED_NONCE_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String REQUEST_TIMEOUT = "300";

    private final Logger logger = LoggerFactory.getLogger(MillheatBridgeHandler.class);

    private @Nullable String userId;

    private @Nullable String token;

    private HttpClient httpClient;

    private RequestLogger requestLogger;

    private MillheatDiscoveryService discoveryService;

    private Gson gson;

    private MillheatModel model = new MillheatModel();

    private @Nullable ScheduledFuture<?> statusFuture;

    private MillheatBridgeConfiguration config;

    private Map<ThingUID, ServiceRegistration<DiscoveryService>> discoveryServiceRegistrations = new HashMap<>();

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i) {
            sb.append(ALLOWED_NONCE_CHARACTERS.charAt(random.nextInt(ALLOWED_NONCE_CHARACTERS.length())));
        }
        return sb.toString();
    }

    public MillheatBridgeHandler(Bridge bridge, HttpClient httpClient, BundleContext context) {
        super(bridge);
        this.httpClient = httpClient;

        this.httpClient.getContentDecoderFactories().clear();
        this.httpClient.setUserAgentField(new HttpField("User-Agent", "MillheatApp"));

        BooleanSerializer serializer = new BooleanSerializer();

        // @formatter:off
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .registerTypeAdapter(Boolean.class, serializer)
                .registerTypeAdapter(boolean.class, serializer)
                .create();
        // @formatter:on
        config = getConfigAs(MillheatBridgeConfiguration.class);

        discoveryService = new MillheatDiscoveryService(this);

        ServiceRegistration<DiscoveryService> serviceRegistration = context.registerService(DiscoveryService.class,
                discoveryService, null);

        discoveryServiceRegistrations.put(this.getThing().getUID(), serviceRegistration);

        requestLogger = new RequestLogger(bridge.getUID().getId());

    }

    private boolean allowModelUpdate() {

        long timeSinceLastUpdate = System.currentTimeMillis() - model.lastUpdated;
        if (timeSinceLastUpdate > MIN_TIME_BETWEEEN_MODEL_UPDATES_MS) {
            return true;
        }
        return false;
    }

    public MillheatModel getModel() {
        return model;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Bridge does not support any commands, but received command " + command + " for channelUID "
                + channelUID);
    }

    private boolean doLogin() {
        try {
            config = getConfigAs(MillheatBridgeConfiguration.class);

            LoginResponse rsp = sendLoginRequest(new LoginRequest(config.username, config.password),
                    LoginResponse.class);
            int errorCode = rsp.errorCode;
            if (0 != errorCode) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("Error login in: code=%s, type=%s, message=%s", errorCode, rsp.errorName,
                                rsp.errorDescription));
            } else {
                // No error provided on login, proceed to find token and userid
                token = StringUtils.trimToNull(rsp.token);
                userId = rsp.userId == null ? null : rsp.userId.toString();
                if (token == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "error login in, no token provided");
                } else if (userId == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "error login in, no userId provided");
                } else {
                    return true;
                }
            }
        } catch (MillheatCommunicationException e) {
            logger.error("Error login", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error login: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void initialize() {

        config = getConfigAs(MillheatBridgeConfiguration.class);

        if (StringUtils.trimToNull(config.username) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "username not configured");
        } else if (StringUtils.trimToNull(config.password) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "password not configured");
        } else {

            scheduler.execute(() -> {

                if (doLogin()) {
                    try {
                        model = refreshModel();
                        updateStatus(ThingStatus.ONLINE);
                        discoveryService.startService();
                        initPolling();

                    } catch (Exception e) {
                        model = new MillheatModel(); // Empty model
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "error fetching initial data " + e.getMessage());
                        logger.error("Error initializing Millheat data", e);

                    }
                }

            });

            logger.debug("Finished initializing!");
        }

    }

    @SuppressWarnings("null")
    @Override
    public void handleRemoval() {
        ServiceRegistration<DiscoveryService> serviceRegistration = discoveryServiceRegistrations
                .get(this.getThing().getUID());
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        discoveryService.stopService();
        stopPolling();
        super.dispose();
    }

    /**
     * starts this things polling future
     */
    private void initPolling() {
        stopPolling();

        statusFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                updateModelFromServerAndUpdateThingStatus();

            } catch (Exception e) {
                logger.warn("Error refreshing model", e);
            }
        }, config.refreshInterval, config.refreshInterval, TimeUnit.SECONDS);

    }

    private <T> T sendLoginRequest(AbstractRequest req, Class<T> responseType) throws MillheatCommunicationException {

        try {
            Request request = httpClient.newRequest(API_ENDPOINT_1 + req.getRequestUrl());

            addStandardHeadersAndPayload(request, req);

            return sendRequest(request, req, responseType);
        } catch (UnsupportedEncodingException e) {
            throw new MillheatCommunicationException("Error building Millheat request", e);
        }

    }

    private <T> T sendLoggedInRequest(AbstractRequest req, Class<T> responseType)
            throws MillheatCommunicationException {

        try {
            Request request = buildLoggedInRequest(req);

            return sendRequest(request, req, responseType);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new MillheatCommunicationException("Error building Millheat request", e);
        }

    }

    @SuppressWarnings("unchecked")
    private <T> T sendRequest(Request request, AbstractRequest req, Class<T> responseType)
            throws MillheatCommunicationException {
        try {

            ContentResponse contentResponse = request.send();
            String responseJson = contentResponse.getContentAsString();
            if (contentResponse.getStatus() == HttpStatus.OK_200) {

                AbstractResponse rsp = (AbstractResponse) gson.fromJson(responseJson, responseType);
                if (rsp == null) {
                    return null;
                } else if (rsp.errorCode == 0) {
                    return (T) rsp;
                } else {
                    throw new MillheatCommunicationException(req, rsp);
                }
            } else {
                throw new MillheatCommunicationException(
                        "Error sending request to Millheat server. Server responded with " + contentResponse.getStatus()
                                + " and payload " + responseJson);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MillheatCommunicationException("Error sending request to Millheat server", e);
        }

    }

    protected MillheatModel refreshModel() throws MillheatCommunicationException {

        MillheatModel model = new MillheatModel();

        GetHomesResponse homesRsp = sendLoggedInRequest(new GetHomesRequest(), GetHomesResponse.class);
        model.homes = new Home[homesRsp.homes.length];
        for (int i = 0; i < model.homes.length; i++) {
            model.homes[i] = new Home(homesRsp.homes[i]);
        }

        for (Home home : model.homes) {
            SelectRoomByHomeResponse roomRsp = sendLoggedInRequest(
                    new SelectRoomByHomeRequest(Long.parseLong(home.id), home.timezone),
                    SelectRoomByHomeResponse.class);
            home.rooms = new Room[roomRsp.rooms.length];
            for (int i = 0; i < home.rooms.length; i++) {
                home.rooms[i] = new Room(roomRsp.rooms[i], home);
            }

            for (Room room : home.rooms) {
                SelectDeviceByRoomResponse deviceRsp = sendLoggedInRequest(
                        new SelectDeviceByRoomRequest(Long.parseLong(room.id), home.timezone),
                        SelectDeviceByRoomResponse.class);
                room.heaters = new Heater[deviceRsp.devices.length];
                for (int i = 0; i < room.heaters.length; i++) {
                    room.heaters[i] = new Heater(deviceRsp.devices[i], room);
                }

            }

            GetIndependentDevicesByHomeResponse independentRsp = sendLoggedInRequest(
                    new GetIndependentDevicesByHomeRequest(Long.parseLong(home.id), home.timezone),
                    GetIndependentDevicesByHomeResponse.class);
            home.independentHeaters = new Heater[independentRsp.devices.length];
            for (int i = 0; i < home.independentHeaters.length; i++) {
                home.independentHeaters[i] = new Heater(independentRsp.devices[i]);
            }

        }

        model.lastUpdated = System.currentTimeMillis();

        return model;
    }

    /**
     * Stops this thing's polling future
     */
    @SuppressWarnings("null")
    private void stopPolling() {

        if (statusFuture != null && !statusFuture.isCancelled()) {
            statusFuture.cancel(true);
            statusFuture = null;
        }
    }

    public void updateModelFromServerAndUpdateThingStatus() {
        if (allowModelUpdate()) {

            boolean success = false;

            int retriesLeft = 2;
            while (retriesLeft > 0) {
                retriesLeft--;
                try {
                    model = refreshModel();
                    updateThingStatuses();
                    success = true;
                } catch (MillheatCommunicationException e) {
                    if (AbstractResponse.ERROR_CODE_ACCESS_TOKEN_EXPIRED == e.getErrorCode()
                            || AbstractResponse.ERROR_CODE_INVALID_SIGNATURE == e.getErrorCode()) {
                        doLogin();
                    }
                }
            }

            if (!success) {
                logger.error("Error updating model from server, giving up");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }

        }
    }

    private void updateThingStatuses() {
        List<Thing> subThings = getThing().getThings();
        for (Thing thing : subThings) {
            ThingHandler handler = thing.getHandler();
            if (handler != null) {
                MillheatBaseThingHandler mHandler = (MillheatBaseThingHandler) handler;
                mHandler.updateState(model);
            }
        }
    }

    private Request buildLoggedInRequest(AbstractRequest req)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String nonce = getRandomString(NUM_NONCE_CHARS);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signatureBasis = REQUEST_TIMEOUT + timestamp + nonce + token;
        @SuppressWarnings("deprecation")
        String signature = DigestUtils.shaHex(signatureBasis);
        String reqJson = gson.toJson(req);

        // @formatter:off

        Request request = httpClient
                .newRequest(API_ENDPOINT_2 + req.getRequestUrl());


        return addStandardHeadersAndPayload(request,req)
            .header("X-Zc-Timestamp",timestamp)
            .header("X-Zc-Timeout", REQUEST_TIMEOUT)
            .header("X-Zc-Nonce", nonce)
            .header("X-Zc-User-Id", userId)
            .header("X-Zc-User-Signature", signature)
            .header("X-Zc-Content-Length", ""+reqJson.length());
        // @formatter:on

    }

    private Request addStandardHeadersAndPayload(Request req, AbstractRequest payload)
            throws UnsupportedEncodingException {
        requestLogger.listenTo(req);

     // @formatter:off
        return
         req.header("Connection", "Keep-Alive")
            .header("X-Zc-Major-Domain", "seanywell")
            .header("X-Zc-Msg-Name", "millService")
            .header("X-Zc-Sub-Domain", "milltype")
            .header("X-Zc-Seq-Id", "1")
            .header("X-Zc-Version", "1")
            .method(HttpMethod.POST)
            .timeout(5, TimeUnit.SECONDS)
            .content(new BytesContentProvider((gson.toJson(payload)).getBytes("UTF-8")), CONTENT_TYPE);
     // @formatter:on
    }

    public void updateRoomTemperature(String roomId, Command command, ModeType mode) {
        Home home = model.findHomeByRoomId(roomId);
        Room room = model.findRoomById(roomId);

        if (home != null && room != null) {
            SetRoomTempRequest req = new SetRoomTempRequest(home, room);

            int newTemp = (int) ((QuantityType<?>) command).longValue();
            switch (mode) {
                case Sleep:
                    req.sleepTemp = newTemp;
                    break;
                case Away:
                    req.awayTemp = newTemp;
                    break;
                case Comfort:
                    req.comfortTemp = newTemp;
                    break;
                default:
                    logger.error("Cannot set room temp for mode " + mode);
            }

            try {
                sendLoggedInRequest(req, SetRoomTempResponse.class);
            } catch (MillheatCommunicationException e) {
                logger.error("Error updating temperature for room " + roomId, e);
            }

        }

    }

    public void updateIndependentHeaterProperties(@Nullable String macAddress, @Nullable String heaterId,
            @Nullable Command temperatureCommand, @Nullable Command masterOnOffCommand, @Nullable Command fanCommand) {
        Heater heater = model.findHeaterByMacOrId(macAddress, heaterId);
        if (heater != null) {

            int setTemp = heater.targetTemp;
            if (temperatureCommand != null) {
                setTemp = (int) ((QuantityType<?>) temperatureCommand).longValue();
            }

            boolean masterOnOff = heater.powerStatus;
            if (masterOnOffCommand != null) {
                masterOnOff = masterOnOffCommand == OnOffType.ON ? true : false;
            }

            boolean fanActive = heater.fanActive;
            if (fanCommand != null) {
                fanActive = fanCommand == OnOffType.ON ? true : false;
            }

            SetDeviceTempRequest req = new SetDeviceTempRequest(heater, setTemp, masterOnOff, fanActive);
            try {
                sendLoggedInRequest(req, SetRoomTempResponse.class);
                heater.targetTemp = setTemp;
                heater.powerStatus = masterOnOff;
                heater.fanActive = fanActive;

            } catch (MillheatCommunicationException e) {
                logger.error("Error updating temperature for heater {}", macAddress, e);
            }
        }
    }

}
