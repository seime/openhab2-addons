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
package org.openhab.binding.august.internal;

import org.junit.jupiter.api.Test;
import org.openhab.binding.august.internal.comm.PubNubListener;
import org.openhab.binding.august.internal.comm.PubNubMessageException;
import org.openhab.binding.august.internal.comm.PubNubMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Simple manual testing helper class to debug/analyse PubNub server push messages
 * Must populate userId and channelName + set breakpoint
 * 
 * @author Arne Seime - Initial contribution
 */
class PubNubMessageSubscriberTest implements PubNubListener {

    private static final Logger logger = LoggerFactory.getLogger(PubNubMessageSubscriberTest.class);

    private String userId = "d1174952-e923-4e76-bc8c-05c6cad5f654";// = "INSERT_USERID_FROM_GET_SESSION_RESPONSE";
    private String channelName = "54aece2f-b021-4362-9d0f-2cdcd400f3e6";// = "INSERT_PUBSUBCHANNEL_FROM_LOCKS_RESPONSE";

    private Gson gson = new Gson();

    @Test
    // @Disabled("Needs userId and channelName populated - for manual testing/analyzing responses from PubNub")
    void testSubscribe() throws PubNubMessageException {

        PubNubMessageSubscriber subscriber = new PubNubMessageSubscriber();
        subscriber.init(userId, this);
        subscriber.addListener(channelName);
        // Intentionally duplicate
        subscriber.addListener(channelName);
        logger.debug("Started listener");

        subscriber.removeListener(channelName);
        // Intentionally duplicate
        subscriber.removeListener(channelName);
        subscriber.dispose();
    }

    @Override
    public void onPushMessage(String channelName, JsonElement message) {
        logger.debug("Message received on channel {}: {}", channelName, gson.toJson(message));
    }

    @Override
    public void onDisconnect(String channelName) {
        logger.debug("Channel {} disconnected", channelName);
    }

    @Override
    public void onConnect(String channelName) {
        logger.debug("Channel {} connected", channelName);
    }
}
