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
public class SagaThermResponseMessage extends SagaThermMessage {

    public static final byte MESSAGE_RESPONSE_MAGIC_NUMBER = (byte) 0xFD;

    public SagaThermResponseMessage(Command command) {
        super(MESSAGE_RESPONSE_MAGIC_NUMBER, command);
    }

    public static SagaThermResponseMessage of(byte[] buffer) {
        if (buffer.length < MESSAGE_HEADER_LENGTH) {
            throw new IllegalArgumentException("Wrong message length");
        }
        if (buffer[0] != MESSAGE_RESPONSE_MAGIC_NUMBER) {
            throw new IllegalArgumentException("Wrong message magic number");
        }

        Command command = Command.valueOf(buffer[1]);
        if (command == null) {
            throw new IllegalArgumentException("Unrecognized command in message");
        }

        int length = buffer[2];

        if (length != buffer.length) {
            throw new IllegalArgumentException("Wrong message length");
        }
        if (buffer[length - 1] != SagaThermMessage.calcCheckSum(buffer)) {
            throw new IllegalArgumentException("Wrong message checksum");
        }

        if (command == Command.READ_STATUS) {
            return SagaThermResponseMessageStatus.of(buffer);
        } else if (command == Command.READ_PARAM) {
            return SagaThermResponseMessageReadParam.of(buffer);
        } else if (command == Command.WRITE_PARAM) {
            return SagaThermResponseMessageWriteParam.of(buffer);
        }

        return new SagaThermResponseMessage(Command.NONE);
    }
}
