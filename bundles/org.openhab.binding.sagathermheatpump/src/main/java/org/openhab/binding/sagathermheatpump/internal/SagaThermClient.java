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
package org.openhab.binding.sagathermheatpump.internal;

import static org.openhab.binding.sagathermheatpump.internal.SagaThermMessage.ParameterIndex;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maxim Yelgazin - Initial contribution
 */
@NonNullByDefault
public class SagaThermClient extends SagaThermClientBase {
    protected final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("thingHandler");
    private final Logger logger = LoggerFactory.getLogger(SagaThermClient.class);
    private final ScheduledFuture<?> pollingStatusJob;
    private final SagaThermClientCallback callback;

    public SagaThermClient(String hostname, int port, int pollInterval, SagaThermClientCallback callback) {
        super(hostname, port);
        this.callback = callback;
        pollingStatusJob = scheduler.scheduleWithFixedDelay(this::pollingDeviceStatus, 0, pollInterval,
                TimeUnit.SECONDS);
        logger.debug("Started polling request status job");
    }

    @Override
    public void disconnect() {
        pollingStatusJob.cancel(true);
        super.disconnect();
    }

    private void pollingDeviceStatus() {
        if (isConnected()) {
            logger.debug("Polling heatpump status");
            try {
                requestStatus();
            } catch (Exception ex) {
                logger.error("Error requesting status. {}", ex.getMessage());
                onUncaughtException(ex);
            }
        } else {
            logger.debug("Polling heatpump status. Not yet connected");
        }
    }

    @Override
    protected void processMessage(SagaThermResponseMessage message) {
        if (isConnected()) {
            if (message instanceof SagaThermResponseMessageReadParam) {
                var extendedMessage = (SagaThermResponseMessageReadParam) message;
                var parameterIndex = extendedMessage.getParameterIndex();
                if (parameterIndex == ParameterIndex.SETPOINT_COMFORT_TEMPERATURE) {
                    callback.updateSetpointComfortTemperature(extendedMessage.getParameterValue());
                } else if (parameterIndex == ParameterIndex.SETPOINT_BOILER_TEMPERATURE) {
                    callback.updateSetpointBoilerTemperature(extendedMessage.getParameterValue());
                }
            } else if (message instanceof SagaThermResponseMessageWriteParam) {
                var extendedMessage = (SagaThermResponseMessageWriteParam) message;
                var parameterIndex = extendedMessage.getParameterIndex();
                if (parameterIndex == ParameterIndex.SETPOINT_COMFORT_TEMPERATURE) {
                    callback.updateSetpointComfortTemperature(extendedMessage.getParameterValue());
                } else if (parameterIndex == ParameterIndex.SETPOINT_BOILER_TEMPERATURE) {
                    callback.updateSetpointBoilerTemperature(extendedMessage.getParameterValue());
                }
            } else if (message instanceof SagaThermResponseMessageStatus) {
                var extendedMessage = (SagaThermResponseMessageStatus) message;
                callback.updateAirTemperature(extendedMessage.getTemperature(1));
                callback.updateBoilerTemperature(extendedMessage.getTemperature(7));
                callback.updateState(extendedMessage.getState(0));
            }
        }
    }

    @Override
    protected void onUncaughtException(Throwable ex) {
        if (isConnected()) {
            callback.onUncaughtException(ex);
        }
    }

    public void setPower(boolean on) throws IOException {
    }

    public void requestPower() throws IOException {
    }

    public void setBoiler(boolean on) throws IOException {
    }

    public void requestBoiler() throws IOException {
    }

    public void requestStatus() throws IOException {
        var message = new SagaThermRequestMessageStatus();
        sendMessage(message);
    }

    public void setSetpointComfortTemperature(double temperature) throws IOException {
        var message = new SagaThermRequestMessageWriteParam(ParameterIndex.SETPOINT_COMFORT_TEMPERATURE,
                (byte) Math.round(temperature));
        sendMessage(message);
    }

    public void requestSetpointComfortTemperature() throws IOException {
        var message = new SagaThermRequestMessageReadParam(ParameterIndex.SETPOINT_COMFORT_TEMPERATURE);
        sendMessage(message);
    }

    public void setSetpointBoilerTemperature(double temperature) throws IOException {
        var message = new SagaThermRequestMessageWriteParam(ParameterIndex.SETPOINT_BOILER_TEMPERATURE,
                (byte) Math.round(temperature));
        sendMessage(message);
    }

    public void requestSetpointBoilerTemperature() throws IOException {
        var message = new SagaThermRequestMessageReadParam(ParameterIndex.SETPOINT_BOILER_TEMPERATURE);
        sendMessage(message);
    }
}
