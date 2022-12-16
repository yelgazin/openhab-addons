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

import static org.openhab.binding.sagathermheatpump.internal.SagaThermMessage.MESSAGE_HEADER_LENGTH;
import static org.openhab.binding.sagathermheatpump.internal.SagaThermMessage.MESSAGE_MAX_LENGTH;
import static org.openhab.binding.sagathermheatpump.internal.SagaThermResponseMessage.MESSAGE_RESPONSE_MAGIC_NUMBER;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maxim Yelgazin - Initial contribution
 */
@NonNullByDefault
public abstract class SagaThermClientBase {
    private final Logger logger = LoggerFactory.getLogger(SagaThermClientBase.class);
    private Thread socketReceiver = new Thread(this::receiveData);
    private static final int CONNECT_TIMEOUT = 5000;
    private final String hostname;
    private final int port;
    private Socket socket = new Socket();
    private boolean connected;
    private @Nullable DataOutputStream outputStream;

    public SagaThermClientBase(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket();
        socketReceiver = new Thread(this::receiveData);

        InetSocketAddress socketAddress = new InetSocketAddress(hostname, port);
        socket.connect(socketAddress, CONNECT_TIMEOUT);
        outputStream = new DataOutputStream(socket.getOutputStream());
        socketReceiver.setUncaughtExceptionHandler((th, ex) -> {
            if (!(ex instanceof InterruptedException)) {
                onUncaughtException(ex);
            }
        });

        if (!socketReceiver.isAlive()) {
            socketReceiver.start();
        }
        connected = true;
    }

    public void disconnect() {
        connected = false;

        try {
            if (socketReceiver.isAlive()) {
                socketReceiver.interrupt();
            }
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
    }

    protected boolean isConnected() {
        return connected;
    }

    protected synchronized void sendMessage(SagaThermRequestMessage message) throws IOException {
        try {
            if (outputStream != null) {
                outputStream.write(message.toArray());
            } else {
                logger.debug("Output socket stream is not ready yet");
            }
        } catch (IOException ex) {
            disconnect();
            logger.error("Error sending message");
            throw ex;
        }
    }

    private void receiveData() {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            byte[] header = new byte[MESSAGE_HEADER_LENGTH];
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    input.readFully(header, 0, MESSAGE_HEADER_LENGTH);
                    byte magic = header[0];
                    byte length = header[2];

                    if (magic == MESSAGE_RESPONSE_MAGIC_NUMBER
                            && length <= (MESSAGE_MAX_LENGTH - MESSAGE_HEADER_LENGTH)) {
                        byte[] message = new byte[length];
                        System.arraycopy(header, 0, message, 0, header.length);
                        input.readFully(message, MESSAGE_HEADER_LENGTH, length - MESSAGE_HEADER_LENGTH);
                        processMessage(SagaThermResponseMessage.of(message));
                    } else {
                        logger.debug("Received incorrect message");
                    }
                } catch (IllegalArgumentException ex) {
                    logger.debug("Received incorrect message. {}", ex.getMessage());
                }
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            onUncaughtException(ex);
        }
    }

    protected abstract void processMessage(SagaThermResponseMessage message);

    protected abstract void onUncaughtException(Throwable ex);
}
