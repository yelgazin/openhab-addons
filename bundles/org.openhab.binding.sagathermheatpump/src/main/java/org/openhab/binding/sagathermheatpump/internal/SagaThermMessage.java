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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Maxim Yelgazin - Initial contribution
 */

@NonNullByDefault
public abstract class SagaThermMessage {

    // Type + Command + Length
    protected static final byte MESSAGE_HEADER_LENGTH = 3;
    protected static final int MESSAGE_MAX_LENGTH = 255;

    public enum Command {
        NONE(0x00),
        READ_STATUS(0x02),
        READ_PARAM(0x03),
        WRITE_PARAM(0x04),
        WRITE_EEPROM(0x07);

        private final int value;
        private static final Map<Integer, Command> map = new HashMap<>();

        Command(int value) {
            this.value = value;
        }

        static {
            for (Command command : Command.values()) {
                map.put(command.value, command);
            }
        }

        public byte getByte() {
            return (byte) value;
        }

        @Nullable
        public static Command valueOf(int command) {
            return map.get(command);
        }
    }

    public enum ParameterIndex {
        NONE(0),
        SETPOINT_COMFORT_TEMPERATURE(2),
        SETPOINT_BOILER_TEMPERATURE(33);

        private final int value;
        private static final Map<Integer, ParameterIndex> map = new HashMap<>();

        ParameterIndex(int value) {
            this.value = value;
        }

        static {
            for (ParameterIndex parameterIndex : ParameterIndex.values()) {
                map.put(parameterIndex.value, parameterIndex);
            }
        }

        public byte getByte() {
            return (byte) value;
        }

        @Nullable
        public static ParameterIndex valueOf(int parameterIndex) {
            return map.get(parameterIndex);
        }
    }

    protected SagaThermMessage(byte type, Command command) {
        this.type = type;
        this.command = command;
    }

    protected final byte type;
    protected final Command command;

    public static byte calcCheckSum(byte[] message) {
        byte sum = (byte) 0;
        for (int i = 0; i < message.length - 1; i++) {
            sum = (byte) (message[i] + sum);
        }
        return sum;
    }
}
