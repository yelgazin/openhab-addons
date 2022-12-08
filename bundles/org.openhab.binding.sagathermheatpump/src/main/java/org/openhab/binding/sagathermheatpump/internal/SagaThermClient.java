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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Maxim Yelgazin - Initial contribution
 */
@NonNullByDefault
public class SagaThermClient extends SagaThermClientBase implements Observable {

    public SagaThermClient(String hostname, int port) {
        super(hostname, port);
    }

    private final List<Observer> observers = new LinkedList<>();

    @Override
    public void registerObserver(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    protected void processMessage(SagaThermResponseMessage message) {
        if (message instanceof SagaThermResponseMessageReadParam) {
            var extendedMessage = (SagaThermResponseMessageReadParam) message;
            var parameterIndex = extendedMessage.getParameterIndex();
            if (parameterIndex == ParameterIndex.SETPOINT_COMFORT_TEMPERATURE) {
                observers.forEach(x -> x.updateSetpointComfortTemperature(extendedMessage.getParameterValue()));
            } else if (parameterIndex == ParameterIndex.SETPOINT_BOILER_TEMPERATURE) {
                observers.forEach(x -> x.updateSetpointBoilerTemperature(extendedMessage.getParameterValue()));
            }
        } else if (message instanceof SagaThermResponseMessageWriteParam) {
            var extendedMessage = (SagaThermResponseMessageWriteParam) message;
            var parameterIndex = extendedMessage.getParameterIndex();
            if (parameterIndex == ParameterIndex.SETPOINT_COMFORT_TEMPERATURE) {
                observers.forEach(x -> x.updateSetpointComfortTemperature(extendedMessage.getParameterValue()));
            } else if (parameterIndex == ParameterIndex.SETPOINT_BOILER_TEMPERATURE) {
                observers.forEach(x -> x.updateSetpointBoilerTemperature(extendedMessage.getParameterValue()));
            }
        } else if (message instanceof SagaThermResponseMessageStatus) {
            var extendedMessage = (SagaThermResponseMessageStatus) message;
            observers.forEach(x -> {
                x.updateAirTemperature(extendedMessage.getTemperature(1));
                x.updateBoilerTemperature(extendedMessage.getTemperature(7));
                x.updateState(extendedMessage.getState(0));
            });
        }
    }

    @Override
    protected void onUncaughtException(Throwable ex) {
        observers.forEach(x -> x.onUncaughtException(ex));
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
