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
package org.openhab.binding.groheondus.internal.handler;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groheondus.internal.GroheOndusAccountConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.floriansw.ondus.api.OndusService;

/**
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 */
@NonNullByDefault
public class GroheOndusAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(GroheOndusAccountHandler.class);

    private @Nullable OndusService ondusService;
    private @Nullable ScheduledFuture<?> reloginFuture;

    private Instant accessTokenExpiresAt = Instant.now();

    private Instant refreshTokenExpiresAt = Instant.now();

    private static final int REFRESH_TOKEN_TTL_DAYS = 179;

    public GroheOndusAccountHandler(Bridge bridge) {
        super(bridge);
    }

    public OndusService getService() {
        OndusService ret = this.ondusService;
        if (ret == null) {
            throw new IllegalStateException("OndusService requested, which is null (UNINITIALIZED)");
        }
        return ret;
    }

    @Override
    public void initialize() {
        cancelLoginTimer();
        scheduler.schedule(this::login, 0, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (ondusService != null) {
            ondusService = null;
        }
        cancelLoginTimer();
        super.dispose();
    }

    private void cancelLoginTimer() {
        if (reloginFuture != null) {
            reloginFuture.cancel(true);
            reloginFuture = null;
        }
    }

    private void refreshToken() {
        OndusService ondusService = this.ondusService;
        if (ondusService == null) {
            logger.warn("Trying to refresh Ondus account without a service being present.");
            return;
        }
        try {
            logger.debug("Refreshing token");
            ondusService.refreshAuthorization();
            Instant nextRefreshTime = scheduleTokenRefreshing();
            logger.debug("Refreshed token, token expires at {}. Next refresh {}",
                    ondusService.authorizationExpiresAt().atZone(ZoneId.systemDefault()),
                    nextRefreshTime.atZone(ZoneId.systemDefault()));
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            if (refreshTokenExpiresAt.isBefore(Instant.now())) {
                // Refresh token expired, must do new login
                login();
            } else {
                logger.debug("Refreshing token failed, retrying in 1 minute", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Error refreshing token");
                reloginFuture = scheduler.schedule(this::refreshToken, 1, TimeUnit.MINUTES);
            }
        }
    }

    private void login() {
        GroheOndusAccountConfiguration config = getConfigAs(GroheOndusAccountConfiguration.class);
        if (config.username == null || config.password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "@text/error.login.missing.credentials");
        } else {
            try {
                ondusService = OndusService.loginWebform(config.username, config.password);
                Instant refreshTime = scheduleTokenRefreshing();
                logger.debug("Login ok, scheduled token refresh at {}", refreshTime.atZone(ZoneId.systemDefault()));
                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                logger.warn("Error logging in, will retry in 1 minute", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                reloginFuture = scheduler.schedule(this::login, 1, TimeUnit.MINUTES);
            } catch (LoginException e) {
                logger.warn("Error logging in, no retry scheduled", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.login.failed");
            }
        }
    }

    private Instant scheduleTokenRefreshing() {
        accessTokenExpiresAt = ondusService.authorizationExpiresAt();
        refreshTokenExpiresAt = Instant.now().plus(REFRESH_TOKEN_TTL_DAYS, ChronoUnit.DAYS);
        // Refresh 5 minutes before expiry
        Instant refreshTime = accessTokenExpiresAt.minus(5, ChronoUnit.MINUTES);
        Duration durationUntilRefresh = Duration.between(Instant.now(), refreshTime);
        reloginFuture = scheduler.schedule(() -> refreshToken(), durationUntilRefresh.getSeconds(), TimeUnit.SECONDS);
        return refreshTime;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do for bridge
    }
}
