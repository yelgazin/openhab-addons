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

import static org.openhab.binding.sagathermheatpump.internal.SagaThermHeatPumpBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SagaThermHeatPumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Maxim Yelgazin - Initial contribution
 */
@NonNullByDefault
public class SagaThermHeatPumpHandler extends BaseThingHandler implements Observer {
    private final Logger logger = LoggerFactory.getLogger(SagaThermHeatPumpHandler.class);
    private final SagaThermHeatPumpConfiguration config = getConfigAs(SagaThermHeatPumpConfiguration.class);
    private final SagaThermClient client = new SagaThermClient(config.hostname, config.port);
    @Nullable
    private ScheduledFuture<?> poolingStatusJob = null;
    @Nullable
    private ScheduledFuture<?> poolingThinsStatusJob = null;

    public SagaThermHeatPumpHandler(Thing thing) {
        super(thing);
        client.registerObserver(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (CHANNEL_SETPOINT_COMFORT_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    client.setSetpointComfortTemperature(((QuantityType<?>) command).doubleValue());
                } else if (command instanceof RefreshType) {
                    client.requestSetpointComfortTemperature();
                }
            }

            if (CHANNEL_SETPOINT_BOILER_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof QuantityType) {
                    client.setSetpointBoilerTemperature(((QuantityType<?>) command).doubleValue());
                } else if (command instanceof RefreshType) {
                    client.requestSetpointBoilerTemperature();
                }
            }
        } catch (IOException ex) {
            logger.error("Exception while trying to send requests.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void initialize() {
        try {
            client.initialize();
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception ex) {
            logger.error("Could not connect to the device. {}", ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not connect to the device");
        }

        // TO DO
        // Decide what to do with previous jobs
        poolingStatusJob = scheduler.scheduleWithFixedDelay(this::pollingStatus, 0, config.refreshInterval,
                TimeUnit.SECONDS);
        poolingThinsStatusJob = scheduler.scheduleWithFixedDelay(this::poolingThingStatusJob, 0, 5, TimeUnit.MINUTES);
    }

    private void pollingStatus() {
        try {
            client.requestStatus();
        } catch (IOException ignored) {
        }
    }

    private void poolingThingStatusJob() {
        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            initialize();
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        client.terminate();
    }

    @Override
    public void dispose() {
        super.dispose();
        client.removeObserver(this);
        client.dispose();

        if (poolingStatusJob != null) {
            poolingStatusJob.cancel(true);
        }
        if (poolingThinsStatusJob != null) {
            poolingThinsStatusJob.cancel(true);
        }
    }

    @Override
    public void updatePower(boolean on) {
    }

    @Override
    public void updateBoiler(boolean on) {
    }

    @Override
    public void updateState(int state) {
    }

    @Override
    public void updateSetpointComfortTemperature(double temperature) {
        updateState(CHANNEL_SETPOINT_COMFORT_TEMPERATURE, new QuantityType<>(temperature, SIUnits.CELSIUS));
    }

    @Override
    public void updateSetpointBoilerTemperature(double temperature) {
        updateState(CHANNEL_SETPOINT_BOILER_TEMPERATURE, new QuantityType<>(temperature, SIUnits.CELSIUS));
    }

    @Override
    public void updateAirTemperature(double temperature) {
        updateState(CHANNEL_AIR_TEMPERATURE, new QuantityType<>(temperature, SIUnits.CELSIUS));
    }

    @Override
    public void updateBoilerTemperature(double temperature) {
        updateState(CHANNEL_BOILER_TEMPERATURE, new QuantityType<>(temperature, SIUnits.CELSIUS));
    }

    public void onUncaughtException(Throwable ex) {
        updateStatus(ThingStatus.OFFLINE);
        logger.error("Uncaught exception. {}", ex.getMessage());
    }
}
