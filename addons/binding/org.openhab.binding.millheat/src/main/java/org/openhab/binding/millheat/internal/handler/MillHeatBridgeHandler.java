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

import static org.openhab.binding.millheat.internal.MillHeatBindingConstants.CHANNEL_CURRENT_TEMPERATURE;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.millheat.internal.MillHeatBridgeConfiguration;
import org.openhab.binding.millheat.internal.MillHeatDiscoveryService;
import org.openhab.binding.millheat.internal.client.BooleanSerializer;
import org.openhab.binding.millheat.internal.client.RequestLogger;
import org.openhab.binding.millheat.internal.dto.AbstractRequest;
import org.openhab.binding.millheat.internal.dto.AbstractResponse;
import org.openhab.binding.millheat.internal.dto.GetHomesRequest;
import org.openhab.binding.millheat.internal.dto.GetHomesResponse;
import org.openhab.binding.millheat.internal.dto.LoginRequest;
import org.openhab.binding.millheat.internal.dto.LoginResponse;
import org.openhab.binding.millheat.internal.dto.SelectDeviceByRoomRequest;
import org.openhab.binding.millheat.internal.dto.SelectDeviceByRoomResponse;
import org.openhab.binding.millheat.internal.dto.SelectRoomByHomeRequest;
import org.openhab.binding.millheat.internal.dto.SelectRoomByHomeResponse;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link MillHeatBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillHeatBridgeHandler extends BaseBridgeHandler {

    private static final int MIN_TIME_BETWEEEN_MODEL_UPDATES_MS = 30_000;

    private static final int NUM_NONCE_CHARS = 16;

    private static final String CONTENT_TYPE = "application/x-zc-object";

    public static String API_ENDPOINT_1 = "https://eurouter.ablecloud.cn:9005/zc-account/v1/";

    public static String API_ENDPOINT_2 = "http://eurouter.ablecloud.cn:5000/millService/v1/";

    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String REQUEST_TIMEOUT = "300";

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i) {
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return sb.toString();
    }

    private final Logger logger = LoggerFactory.getLogger(MillHeatBridgeHandler.class);

    private @Nullable String userId;

    private @Nullable String token;

    private HttpClient httpClient;

    private RequestLogger requestLogger = new RequestLogger();

    private @Nullable MillHeatDiscoveryService discoveryService;

    private Gson gson;

    private MillheatModel model = new MillheatModel();

    /**
     * Future to poll for status
     */
    private @Nullable ScheduledFuture<?> statusFuture;

    private @Nullable MillHeatBridgeConfiguration config;

    public MillHeatBridgeHandler(Bridge bridge, HttpClient httpClient) {
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
        if (CHANNEL_CURRENT_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    private boolean doLogin() {
        try {
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
        config = getConfigAs(MillHeatBridgeConfiguration.class);

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
                        startDiscovery();
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

    @Override
    public void dispose() {
        stopDiscovery();
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
            if (contentResponse.getStatus() == 200) {

                AbstractResponse rsp = (AbstractResponse) gson.fromJson(responseJson, responseType);
                if (rsp.errorCode == 0) {
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
                home.rooms[i] = new Room(roomRsp.rooms[i]);
            }

            for (Room room : home.rooms) {
                SelectDeviceByRoomResponse deviceRsp = sendLoggedInRequest(
                        new SelectDeviceByRoomRequest(Long.parseLong(room.id), home.timezone),
                        SelectDeviceByRoomResponse.class);
                room.heaters = new Heater[deviceRsp.devices.length];
                for (int i = 0; i < room.heaters.length; i++) {
                    room.heaters[i] = new Heater(deviceRsp.devices[i]);
                }

            }

            /*
             * GetIndependentDevicesByHomeResponse independentRsp = sendLoggedInRequest(
             * new GetIndependentDevicesByHomeRequest(Long.parseLong(home.id), home.timezone),
             * GetIndependentDevicesByHomeResponse.class);
             * home.independentHeaters = new Heater[independentRsp.devices.length];
             * for (int i = 0; i < home.independentHeaters.length; i++) {
             * home.independentHeaters[i] = new Heater(independentRsp.devices[i]);
             * }
             */
        }

        model.lastUpdated = System.currentTimeMillis();

        return model;
    }

    public void setDiscoveryService(MillHeatDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    private void startDiscovery() {
        if (discoveryService != null) {
            discoveryService.startService();
        }
    }

    public void stopDiscovery() {
        if (discoveryService != null) {
            discoveryService.stopService();
        }

    }

    /**
     * Stops this thing's polling future
     */
    private void stopPolling() {

        if (statusFuture != null && !statusFuture.isCancelled()) {
            statusFuture.cancel(true);
            statusFuture = null;
        }
    }

    public void updateModelFromServerAndUpdateThingStatus() {
        if (allowModelUpdate()) {

            int retriesLeft = 2;
            while (retriesLeft > 0) {
                retriesLeft--;
                try {
                    model = refreshModel();
                    updateThingStatuses();
                } catch (MillheatCommunicationException e) {
                    if (e.getErrorCode() == AbstractResponse.ERROR_CODE_ACCESS_TOKEN_EXPIRED) {
                        doLogin();
                    }
                }
            }

            if (retriesLeft <= 0) {
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

}
