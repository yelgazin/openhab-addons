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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Maxim Yelgazin - Initial contribution
 */

@NonNullByDefault
public class SagaThermResponseMessageStatus extends SagaThermResponseMessage {

    private final double[] temperatures = new double[16];
    private final double[] pressures = new double[16];
    private final double[] flows = new double[16];
    private final short[] inputs = new short[16];
    private final short[] outputs = new short[16];
    private final short[] errors = new short[16];
    private final boolean[] outlets = new boolean[16];
    private final short[] states = new short[16];

    public SagaThermResponseMessageStatus() {
        super(Command.READ_STATUS);
    }

    public double getTemperature(int index) {
        return temperatures[index];
    }

    public double getPressure(int index) {
        return pressures[index];
    }

    public double getFlow(int index) {
        return flows[index];
    }

    public double getInput(int index) {
        return inputs[index];
    }

    public double getOutput(int index) {
        return outputs[index];
    }

    public double getError(int index) {
        return errors[index];
    }

    public boolean getOutlet(int index) {
        return outlets[index];
    }

    public short getState(int index) {
        return states[index];
    }

    public static SagaThermResponseMessageStatus of(byte[] buffer) {
        var message = new SagaThermResponseMessageStatus();

        short[] data = new short[256];
        Arrays.fill(data, Short.MIN_VALUE);
        for (int i = MESSAGE_HEADER_LENGTH; i < buffer.length - 1; i += 3) {
            int index = buffer[i] & 0xFF;
            data[index] = (short) (((buffer[i + 2] & 0xFF) << 8) | (buffer[i + 1] & 0xFF));
        }

        for (int i = 0; i < 16; i++) {
            message.temperatures[i] = data[i] * 0.0625;
            message.pressures[i] = data[16 + i] * 0.0625;
            message.flows[i] = data[32 + i] * 0.0625;
            message.inputs[i] = data[48 + i];
            message.outputs[i] = data[64 + i];
            message.errors[i] = data[80 + i];
            message.states[i] = data[96 + i];
            message.outlets[i] = (data[64] & 1 << i) > 0;
        }

        return message;
    }
}
