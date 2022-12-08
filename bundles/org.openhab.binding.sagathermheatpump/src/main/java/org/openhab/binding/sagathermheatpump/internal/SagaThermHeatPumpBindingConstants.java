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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SagaThermHeatPumpBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Maxim Yelgazin - Initial contribution
 */
@NonNullByDefault
public class SagaThermHeatPumpBindingConstants {

    private static final String BINDING_ID = "sagathermheatpump";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HEATPUMP = new ThingTypeUID(BINDING_ID, "heat-pump");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_BOILER = "boiler";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_SETPOINT_COMFORT_TEMPERATURE = "setpointComfortTemperature";
    public static final String CHANNEL_SETPOINT_BOILER_TEMPERATURE = "setpointBoilerTemperature";
    public static final String CHANNEL_AIR_TEMPERATURE = "airTemperature";
    public static final String CHANNEL_BOILER_TEMPERATURE = "boilerTemperature";
}
