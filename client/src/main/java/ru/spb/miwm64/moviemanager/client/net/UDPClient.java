package ru.spb.miwm64.moviemanager.client.net;

import ru.spb.miwm64.moviemanager.client.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UDPClient implements ConnectionClient {
    private static final Logger LOG = LoggerFactory.getLogger(UDPClient.class);
    private static final int MAX_PACKET_SIZE = 65536;
    private static final int DEFAULT_RESPONSE_TIMEOUT = 5000;

    private final SocketAddress socketAddress;
    private DatagramSocket socket;
    private boolean isConnected;
    private int responseTimeout = 5000;

    public UDPClient(SocketAddress socketAddress) throws NotConnectedException {
        this(socketAddress, DEFAULT_RESPONSE_TIMEOUT);
    }

    public UDPClient(SocketAddress socketAddress, int responseTimeout) throws NotConnectedException {
        this.responseTimeout = responseTimeout;
        try {
            this.socketAddress = socketAddress;
            this.socket = new DatagramSocket();
            this.isConnected = true;
            this.socket.connect(socketAddress);
            this.socket.setSoTimeout(responseTimeout);
            LOG.info("UDPClient connected to {}", socketAddress);
        } catch (SocketException e) {
            this.isConnected = false;
            LOG.error("Failed to connect UDPClient to {}", socketAddress, e);
            throw new NotConnectedException(e);
        }
    }

    @Override
    public String exchangeString(String msg) throws NetException {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        if (!isConnected()) {
            throw new NotConnectedException();
        }

        try {
            LOG.info("Sending packet to {}", socketAddress);
            byte[] outBuffer = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, socketAddress);
            socket.send(outPacket);

            byte[] inBuffer = new byte[MAX_PACKET_SIZE];
            DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);

            socket.receive(inPacket);
            String response = new String(inPacket.getData(), 0, inPacket.getLength(), StandardCharsets.UTF_8);
            LOG.info("Packet received from {}", socketAddress);
            return response;

        } catch (SocketTimeoutException e) {
            LOG.error("Response timeout from {}", socketAddress, e);
            throw new ResponseTimeoutException(responseTimeout);
        } catch (IOException e) {
            LOG.error("Failed to receive/send packet from/to {}", socketAddress, e);
            throw new NetException("I/O error during UDP exchange", e);
        } finally {
            MDC.remove("requestId");
        }
    }

    @Override
    public void sendOnlyString(String msg) throws NetException {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        if (!isConnected()) {
            throw new NotConnectedException();
        }

        try {
            LOG.info("Sending packet (no response) to {}", socketAddress);
            byte[] outBuffer = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, socketAddress);
            socket.send(outPacket);
        } catch (IOException e) {
            LOG.error("Failed to send packet to {}", socketAddress, e);
            throw new SendFailedException(e);
        } finally {
            MDC.remove("requestId");
        }
    }

    @Override
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            isConnected = false;
            LOG.info("UDPClient socket closed for {}", socketAddress);
        }
    }

    @Override
    public void disconnect() {
        if (socket != null && !socket.isClosed()) {
            socket.disconnect();
            isConnected = false;
            LOG.info("UDPClient disconnected from {}", socketAddress);
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void reconnect() throws NotConnectedException {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            socket = new DatagramSocket();
            socket.connect(socketAddress);
            socket.setSoTimeout(responseTimeout);
            isConnected = true;
            LOG.info("UDPClient reconnected to {}", socketAddress);
        } catch (SocketException e) {
            LOG.error("UDPClient failed to reconnect to {}", socketAddress, e);
            throw new NotConnectedException(e);
        }
    }

    @Override
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    @Override
    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }
}