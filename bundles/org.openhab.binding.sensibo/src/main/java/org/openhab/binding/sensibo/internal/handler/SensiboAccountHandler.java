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
package org.openhab.binding.sensibo.internal.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sensibo.internal.SensiboCommunicationException;
import org.openhab.binding.sensibo.internal.client.RequestLogger;
import org.openhab.binding.sensibo.internal.config.SensiboAccountConfiguration;
import org.openhab.binding.sensibo.internal.discovery.SensiboDiscoveryService;
import org.openhab.binding.sensibo.internal.dto.AbstractRequest;
import org.openhab.binding.sensibo.internal.dto.poddetails.GetPodsDetailsRequest;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetails;
import org.openhab.binding.sensibo.internal.dto.pods.GetPodsRequest;
import org.openhab.binding.sensibo.internal.dto.pods.Pod;
import org.openhab.binding.sensibo.internal.model.AcState;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.openhab.binding.sensibo.internal.model.SensiboSky;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The {@link SensiboAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
public class SensiboAccountHandler extends BaseBridgeHandler {
    public static String API_ENDPOINT = "https://home.sensibo.com/api/v2";
    private static final int MIN_TIME_BETWEEEN_MODEL_UPDATES_MS = 30_000;
    private final Logger logger = LoggerFactory.getLogger(SensiboAccountHandler.class);
    private final HttpClient httpClient;
    private RequestLogger requestLogger;
    private SensiboDiscoveryService discoveryService;
    private Gson gson;
    private SensiboModel model = new SensiboModel(0);
    private @Nullable ScheduledFuture<?> statusFuture;
    private SensiboAccountConfiguration config;
    private Map<ThingUID, ServiceRegistration<DiscoveryService>> discoveryServiceRegistrations = new HashMap<>();

    public SensiboAccountHandler(Bridge bridge, HttpClient httpClient, BundleContext context) {
        super(bridge);
        this.httpClient = httpClient;
        this.httpClient.getContentDecoderFactories().clear();
        this.httpClient.setUserAgentField(new HttpField("User-Agent", "SensiboApp"));

        gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                out.value(value.toString());
            }

            @Override
            public ZonedDateTime read(JsonReader in) throws IOException {
                return ZonedDateTime.parse(in.nextString());
            }

        }).setPrettyPrinting().create();

        config = getConfigAs(SensiboAccountConfiguration.class);
        discoveryService = new SensiboDiscoveryService(this);
        ServiceRegistration<DiscoveryService> serviceRegistration = context.registerService(DiscoveryService.class,
                discoveryService, null);
        discoveryServiceRegistrations.put(this.getThing().getUID(), serviceRegistration);
        requestLogger = new RequestLogger(bridge.getUID().getId());
    }

    private boolean allowModelUpdate() {
        long timeSinceLastUpdate = System.currentTimeMillis() - model.getLastUpdated();
        if (timeSinceLastUpdate > MIN_TIME_BETWEEEN_MODEL_UPDATES_MS) {
            return true;
        }
        return false;
    }

    @NonNull
    public SensiboModel getModel() {
        return model;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Bridge does not support any commands, but received command " + command + " for channelUID "
                + channelUID);
    }

    @Override
    public void initialize() {
        config = getConfigAs(SensiboAccountConfiguration.class);
        scheduler.execute(() -> {
            try {
                model = refreshModel();
                updateStatus(ThingStatus.ONLINE);
                discoveryService.startService();
                initPolling();
            } catch (Exception e) {
                model = new SensiboModel(0); // Empty model
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "error fetching initial data " + e.getMessage());
                logger.info("Error initializing Sensibo data", e);
            }
        });
        logger.debug("Finished initializing!");
    }

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
                logger.debug("Error refreshing model", e);
            }
        }, config.refreshInterval, config.refreshInterval, TimeUnit.SECONDS);
    }

    protected SensiboModel refreshModel()
            throws SensiboCommunicationException, NoSuchAlgorithmException, UnsupportedEncodingException {
        SensiboModel model = new SensiboModel(System.currentTimeMillis());

        GetPodsRequest getPodsRequest = new GetPodsRequest();
        List<Pod> pods = sendRequest(buildGetPodsRequest(getPodsRequest), getPodsRequest,
                new TypeToken<ArrayList<Pod>>() {
                }.getType());

        for (Pod pod : pods) {
            GetPodsDetailsRequest getPodsDetailsRequest = new GetPodsDetailsRequest(pod.getId());

            PodDetails podDetails = sendRequest(buildGetPodDetailsRequest(getPodsDetailsRequest), getPodsDetailsRequest,
                    new TypeToken<PodDetails>() {
                    }.getType());

            model.addPod(new SensiboSky(podDetails));
        }

        return model;

    }

    private <T> T sendRequest(Request request, AbstractRequest req, Type responseType)
            throws SensiboCommunicationException {
        try {
            ContentResponse contentResponse = request.send();
            String responseJson = contentResponse.getContentAsString();
            if (contentResponse.getStatus() == HttpStatus.OK_200) {
                JsonParser parser = new JsonParser();
                JsonObject o = parser.parse(responseJson).getAsJsonObject();
                String overallStatus = o.get("status").getAsString();
                if ("success".equals(overallStatus)) {
                    return gson.fromJson(o.get("result"), responseType);
                } else {
                    throw new SensiboCommunicationException(req, overallStatus);
                }
            } else {
                throw new SensiboCommunicationException(
                        "Error sending request to Sensibo server. Server responded with " + contentResponse.getStatus()
                                + " and payload " + responseJson);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new SensiboCommunicationException("Error sending request to Sensibo server", e);
        }
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
                } catch (SensiboCommunicationException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    logger.debug("Error updating Sensibo model do to {}, retries left {}", e.getMessage(), retriesLeft);
                }
            }
            if (!success) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    }

    private void updateThingStatuses() {
        List<Thing> subThings = getThing().getThings();
        for (Thing thing : subThings) {
            ThingHandler handler = thing.getHandler();
            if (handler != null) {
                SensiboBaseThingHandler mHandler = (SensiboBaseThingHandler) handler;
                mHandler.updateState(model);
            }
        }
    }

    private Request buildGetPodsRequest(GetPodsRequest req)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Request request = buildRequest(req, null);

        return request;
    }

    private Request buildGetPodDetailsRequest(GetPodsDetailsRequest getPodsDetailsRequest)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Request req = buildRequest(getPodsDetailsRequest, null);
        req.param("fields", "*");

        return req;
    }

    private Request buildRequest(AbstractRequest req, Type type)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        Request request = httpClient.newRequest(API_ENDPOINT + req.getRequestUrl()).param("apiKey", config.apiKey)
                .method(req.getMethod());

        if (req.getMethod() == HttpMethod.POST) {
            String reqJson = gson.toJson(req, type);

            request = request.content(new BytesContentProvider((gson.toJson(reqJson)).getBytes("UTF-8")));
        }

        requestLogger.listenTo(request);

        return request;

    }

    public void updateSensiboSkyAcState(@Nullable String macAddress, AcState newState) {
        /*
         * Optional<Heater> optionalHeater = model.findHeaterByMacOrId(macAddress, heaterId);
         * if (optionalHeater.isPresent()) {
         * Heater heater = optionalHeater.get();
         * int setTemp = heater.getTargetTemp();
         * if (temperatureCommand != null && temperatureCommand instanceof QuantityType<?>) {
         * setTemp = (int) ((QuantityType<?>) temperatureCommand).longValue();
         * }
         * boolean masterOnOff = heater.isPowerStatus();
         * if (masterOnOffCommand != null) {
         * masterOnOff = masterOnOffCommand == OnOffType.ON ? true : false;
         * }
         * boolean fanActive = heater.isFanActive();
         * if (fanCommand != null) {
         * fanActive = fanCommand == OnOffType.ON ? true : false;
         * }
         * SetDeviceTempRequest req = new SetDeviceTempRequest(heater, setTemp, masterOnOff, fanActive);
         * try {
         * sendLoggedInRequest(req, SetRoomTempResponse.class);
         * heater.setTargetTemp(setTemp);
         * heater.setPowerStatus(masterOnOff);
         * heater.setFanActive(fanActive);
         * } catch (SensiboCommunicationException e) {
         * logger.info("Error updating temperature for heater {}", macAddress, e);
         * }
         * }
         * }
         */
    }

}
