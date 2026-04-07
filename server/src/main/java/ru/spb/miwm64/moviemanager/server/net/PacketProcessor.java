package ru.spb.miwm64.moviemanager.server.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcError;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcResponse;
import ru.spb.miwm64.moviemanager.server.Main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketProcessor {
    private static final int MAX_PACKET_SIZE = 65536;

    private final UDPTransport transport;
    private final JsonRpc jsonRpc;
    private final RequestRouter handler;

    private final CacheManager cache = new CacheManager();

    private static final Logger LOG = LoggerFactory.getLogger(PacketProcessor.class);

    public PacketProcessor(UDPTransport transport,
                           JsonRpc codec,
                           RequestRouter handler) {
        this.transport = transport;
        this.jsonRpc = codec;
        this.handler = handler;
        LOG.debug("PacketProcessor initialized");
    }

    public void process() {
        SocketAddress client = null;
        Integer id = null;
        UUID uuid = null;

        try {
            LOG.debug("Receiving packet");

            ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
            client = transport.receive(buffer);

            if (client == null) {
                LOG.debug("No packet received");
                return;
            }

            String json = extract(buffer);
            LOG.debug("Raw JSON received: {}", json);

            JsonRpcRequest request = jsonRpc.decodeRequest(json);
            id = request.id;
            uuid = request.uuid;

            if (!(client instanceof InetSocketAddress inetClient)) {
                LOG.warn("Unknown client address type: {}", client.getClass());
                return;
            }

            String ip = inetClient.getAddress().getHostAddress();
            int port = inetClient.getPort();

            RequestKey key = new RequestKey(id, uuid);

            // Check cache for duplicates
            LOG.info("Checking packet for duplication id={} to {}", id, uuid);
            JsonRpcResponse<?> cached = cache.lookUp(key);
            if (cached != null) {
                LOG.info("Duplicate request detected, sending cached response for id={} to {}", id, uuid);
                transport.send(client, jsonRpc.encodePacket(cached));
                return;
            }

            LOG.info("Processing request id={} method={}", id, request.method);
            Object result = handler.route(request.method, request.params);

            // Encode and send response
            byte[] response = jsonRpc.encodeSuccess(result, id, uuid);
            transport.send(client, response);

            // Store in cache for duplicate detection
            cache.add(key, new JsonRpcResponse<>() {{
                this.id = id;
                this.result = result;
            }});
            LOG.info("Added response to cache with id={}, ip:port={}:{}", id, ip, port);
        } catch (Exception e) {
            LOG.error("Error during packet processing (id={})", id, e);

            if (client != null) {
                try {
                    byte[] err = jsonRpc.encodeError(
                            JsonRpcError.INTERNAL_ERROR,
                            "Internal error",
                            id,
                            uuid
                    );
                    transport.send(client, err);
                    LOG.info("Error response sent to {} for id={}", client, id);
                } catch (IOException err) {
                    LOG.error("Failed to send error response", err);
                }
            }
        }
    }


    private String extract(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String result = new String(bytes, StandardCharsets.UTF_8);
        LOG.debug("Extracted {} bytes from buffer", bytes.length);
        return result;
    }
}