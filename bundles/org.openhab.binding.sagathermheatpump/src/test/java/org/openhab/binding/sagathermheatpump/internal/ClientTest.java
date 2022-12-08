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

import static java.lang.Thread.sleep;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Maxim Yelgazin - Initial contribution
 */
@NonNullByDefault
public class ClientTest {

    @Test
    public void testInitialize1() throws InterruptedException, IOException {
        SagaThermClient client = new SagaThermClient("192.168.12.11", 4002);
        client.registerObserver(new Observer() {
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
                System.out.println(String.format("updateSetPointComfortTemperature %.2f", temperature));
            }

            @Override
            public void updateSetpointBoilerTemperature(double temperature) {
                System.out.println(String.format("updateSetPointBoilerTemperature %.2f", temperature));
            }

            @Override
            public void updateAirTemperature(double temperature) {
                System.out.println(String.format("updateAirCurrentTemperature %.2f", temperature));
            }

            @Override
            public void updateBoilerTemperature(double temperature) {
                System.out.println(String.format("updateBoilerCurrentTemperature %.2f", temperature));
            }

            @Override
            public void onUncaughtException(Throwable th) {
            }
        });
        client.initialize();
        sleep(5000);
    }
}
