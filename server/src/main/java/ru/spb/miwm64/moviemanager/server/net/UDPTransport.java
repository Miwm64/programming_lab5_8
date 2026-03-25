package ru.spb.miwm64.moviemanager.server.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.server.Main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPTransport {
    private final DatagramChannel channel;
    private Logger log = LoggerFactory.getLogger(Main.class);

    private static final Logger LOG = LoggerFactory.getLogger(UDPTransport.class);

    public UDPTransport(int port) throws IOException {
        LOG.debug("Opening DatagramChannel");
        channel = DatagramChannel.open();

        LOG.debug("Binding to port {}", port);
        channel.bind(new InetSocketAddress(port));

        LOG.debug("Configuring non-blocking mode");
        channel.configureBlocking(false);

        LOG.info("UDPTransport initialized on port {}", port);
    }

    public SocketAddress receive(ByteBuffer buffer) throws IOException {
        LOG.debug("Receiving packet");
        SocketAddress address = channel.receive(buffer);

        if (address != null) {
            LOG.info("Packet received from {}", address);
        }

        return address;
    }

    public void send(SocketAddress client, byte[] data) throws IOException {
        LOG.debug("Sending packet to {} ({} bytes)", client, data.length);
        channel.send(ByteBuffer.wrap(data), client);
        LOG.info("Packet sent to {}", client);
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    public void close() throws IOException {
        LOG.info("Closing UDPTransport");
        channel.close();
        LOG.info("UDPTransport closed");
    }
}