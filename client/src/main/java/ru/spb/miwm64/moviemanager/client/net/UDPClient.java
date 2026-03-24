package ru.spb.miwm64.moviemanager.client.net;
import ru.spb.miwm64.moviemanager.client.exceptions.*;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UDPClient implements ConnectionClient {
    private static final int MAX_PACKET_SIZE = 65536;
    private static final int DEFAULT_RESPONSE_TIMEOUT = 5000;

    private final SocketAddress socketAddress;
    private DatagramSocket socket;
    private boolean isConnected;
    private int responseTimeout = 5000;

    public UDPClient(SocketAddress socketAddress) throws NotConnectedException {
        this(socketAddress, DEFAULT_RESPONSE_TIMEOUT);  // Call the main constructor
    }

    public UDPClient(SocketAddress socketAddress, int responseTimeout) throws NotConnectedException {
        this.responseTimeout = responseTimeout;
        try {
            this.socketAddress = socketAddress;
            this.socket = new DatagramSocket();
            this.isConnected = true;
            this.socket.connect(socketAddress);
            this.socket.setSoTimeout(responseTimeout);
        } catch (SocketException e) {
            this.isConnected = false;
            throw new NotConnectedException(e);
        }
    }

    @Override
    public String exchangeString(String msg) throws NetException {
        if (!isConnected()) {
            throw new NotConnectedException();
        }

        try {
            byte[] outBuffer = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, socketAddress);

            try {
                socket.send(outPacket);
            } catch (IOException e) {
                throw new SendFailedException(e);
            }

            byte[] inBuffer = new byte[MAX_PACKET_SIZE];
            DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);

            try {
                socket.receive(inPacket);
            } catch (SocketTimeoutException e) {
                throw new ResponseTimeoutException();
            } catch (IOException e) {
                throw new ReceiveFailedException(e);
            }

            String response = new String(
                    inPacket.getData(),
                    0,
                    inPacket.getLength(),
                    StandardCharsets.UTF_8
            );

            return response;

        } catch (NetException e) {
            throw e;
        } catch (Exception e) {
            throw new NetException("Unexpected error during communication: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendOnlyString(String msg) throws NetException {
        if (!isConnected()) {
            throw new NotConnectedException();
        }

        try {
            byte[] outBuffer = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, socketAddress);

            try {
                socket.send(outPacket);
            } catch (IOException e) {
                throw new SendFailedException(e);
            }
        } catch (NetException e) {
            throw e;
        } catch (Exception e) {
            throw new NetException("Unexpected error during communication: " + e.getMessage(), e);
        }
    }


    @Override
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            isConnected = false;
        }
    }

    @Override
    public void disconnect() {
        if (socket != null && !socket.isClosed()) {
            socket.disconnect();
            isConnected = false;
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected && socket != null && socket.isConnected() && !socket.isClosed();
    }


    @Override
    public void reconnect() throws NotConnectedException {
        isConnected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            socket = new DatagramSocket();
            socket.connect(socketAddress);
            socket.setSoTimeout(responseTimeout);
            isConnected = true;
        } catch (SocketException e) {
            throw new NotConnectedException(e);
        }
    }

    @Override
    public SocketAddress getSocketAddress(){
        return socketAddress;
    }

    @Override
    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }
}
