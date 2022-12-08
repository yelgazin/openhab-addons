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
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Maxim Yelgazin - Initial contribution
 */

@NonNullByDefault
public class SagaThermResponseMessageReadParam extends SagaThermResponseMessage {

    @Nullable
    protected ParameterIndex parameterIndex;
    protected byte parameterValue;

    public SagaThermResponseMessageReadParam() {
        super(Command.READ_PARAM);
    }

    @Nullable
    public ParameterIndex getParameterIndex() {
        return parameterIndex;
    }

    public byte getParameterValue() {
        return parameterValue;
    }

    public static SagaThermResponseMessageReadParam of(byte[] buffer) {
        var message = new SagaThermResponseMessageReadParam();
        message.parameterIndex = ParameterIndex.valueOf(buffer[3]);
        message.parameterValue = buffer[5];
        return message;
    }
}
