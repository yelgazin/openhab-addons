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

/**
 * @author Maxim Yelgazin - Initial contribution
 */
@NonNullByDefault
interface SagaThermClientCallback {
    void updatePower(boolean on);

    void updateBoiler(boolean on);

    void updateState(int state);

    void updateSetpointComfortTemperature(double temperature);

    void updateSetpointBoilerTemperature(double temperature);

    void updateAirTemperature(double temperature);

    void updateBoilerTemperature(double temperature);

    void onUncaughtException(Throwable th);
}
