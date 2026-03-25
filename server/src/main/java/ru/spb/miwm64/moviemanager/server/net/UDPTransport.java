package ru.spb.miwm64.moviemanager.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPTransport {
    private final DatagramChannel channel;

    public UDPTransport(int port) throws IOException {
        channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
    }

    public SocketAddress receive(ByteBuffer buffer) throws IOException {
        return channel.receive(buffer);
    }

    public void send(SocketAddress client, byte[] data) throws IOException {
        channel.send(ByteBuffer.wrap(data), client);
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    public void close() throws IOException {
        channel.close();
    }
}