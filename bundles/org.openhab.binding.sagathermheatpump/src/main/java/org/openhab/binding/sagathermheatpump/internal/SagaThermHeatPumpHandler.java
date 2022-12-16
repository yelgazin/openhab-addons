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
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.*;
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
public class SagaThermHeatPumpHandler extends BaseThingHandler implements SagaThermClientCallback {
    private final Logger logger = LoggerFactory.getLogger(SagaThermHeatPumpHandler.class);
    private final SagaThermHeatPumpConfiguration config = getConfigAs(SagaThermHeatPumpConfiguration.class);
    @Nullable
    private SagaThermClient client;
    @Nullable
    private ScheduledFuture<?> pollingThingStatusJob;

    public SagaThermHeatPumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        for (var entry : configurationParameters.entrySet()) {
            switch (entry.getKey()) {
                case "hostname":
                    config.hostname = (String) entry.getValue();
                    break;
                case "port":
                    config.port = ((BigDecimal) entry.getValue()).intValue();
                    break;
                case "pollInterval":
                    config.pollInterval = ((BigDecimal) entry.getValue()).intValue();
                    break;
                case "reconnectInterval":
                    config.reconnectInterval = ((BigDecimal) entry.getValue()).intValue();
                    break;
            }
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing");

        if (pollingThingStatusJob == null) {
            pollingThingStatusJob = scheduler.scheduleWithFixedDelay(this::pollingThingStatus, config.reconnectInterval,
                    config.reconnectInterval, TimeUnit.MINUTES);
        }

        try {
            client = new SagaThermClient(config.hostname, config.port, config.pollInterval, this);
            client.connect();
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception ex) {
            logger.error("Could not connect to the device. {}", ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not connect to the device");
        }
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
            logger.error("IOException while trying to send requests");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        if (status == ThingStatus.OFFLINE) {
            client.disconnect();
        }
        super.updateStatus(status, statusDetail, description);
    }

    private void pollingThingStatus() {
        logger.debug("Polling thing status");
        var thing = getThing();
        if (thing.getStatus() == ThingStatus.OFFLINE
                && thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
            initialize();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing");
        if (pollingThingStatusJob != null) {
            pollingThingStatusJob.cancel(true);
        }
        client.disconnect();
        super.dispose();
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

    @Override
    public void onUncaughtException(Throwable ex) {
        updateStatus(ThingStatus.OFFLINE);
        logger.error("Uncaught exception. {}", ex.getMessage());
    }
}
