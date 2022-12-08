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
public class SagaThermRequestMessageStatus extends SagaThermRequestMessage {

    public SagaThermRequestMessageStatus() {
        super(Command.READ_STATUS);
    }

    @Override
    public byte[] toArray() {
        // Header length + checksum
        int length = MESSAGE_HEADER_LENGTH + 1;

        byte[] buffer = new byte[length];
        buffer[0] = type;
        buffer[1] = command.getByte();
        buffer[2] = (byte) length;
        buffer[length - 1] = calcCheckSum(buffer);
        return buffer;
    }
}
