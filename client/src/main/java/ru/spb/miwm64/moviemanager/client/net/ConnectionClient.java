package ru.spb.miwm64.moviemanager.client.net;

import ru.spb.miwm64.moviemanager.client.exceptions.*;

import java.net.SocketAddress;

public interface ConnectionClient extends AutoCloseable {

    String exchangeString(String msg) throws NetException;
    void sendOnlyString(String msg) throws NetException;

    @Override
    void close();

    void disconnect();

    boolean isConnected();

    void reconnect() throws NotConnectedException;

    SocketAddress getSocketAddress();

    boolean isClosed();
}