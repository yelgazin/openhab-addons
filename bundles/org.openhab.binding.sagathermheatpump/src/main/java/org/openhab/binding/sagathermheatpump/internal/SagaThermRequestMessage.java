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
public class SagaThermRequestMessage extends SagaThermMessage {

    public static final byte MESSAGE_REQUEST_MAGIC_NUMBER = (byte) 0xA6;

    public SagaThermRequestMessage(Command command) {
        super(MESSAGE_REQUEST_MAGIC_NUMBER, command);
    }

    public byte[] toArray() {
        throw new UnsupportedOperationException("Method should be overridden");
    }
}
