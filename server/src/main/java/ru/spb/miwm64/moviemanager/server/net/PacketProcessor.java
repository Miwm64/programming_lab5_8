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

public class PacketProcessor {

    private final UDPTransport transport;
    private final JsonRpc jsonRpc;
    private final RequestHandler handler;

    private CacheManager cache = new CacheManager();

    private static final Logger LOG = LoggerFactory.getLogger(PacketProcessor.class);

    public PacketProcessor(UDPTransport transport,
                           JsonRpc codec,
                           RequestHandler handler) {
        this.transport = transport;
        this.jsonRpc = codec;
        this.handler = handler;
        LOG.debug("PacketProcessor initialized");
    }

    public void process() {
        SocketAddress client = null;
        Integer id = null;

        try {
            LOG.debug("Receiving packet");

            ByteBuffer buffer = ByteBuffer.allocate(65536);
            client = transport.receive(buffer);

            if (client == null) {
                LOG.debug("No packet received");
                return;
            }

            LOG.info("Packet received from {}", client);

            String json = extract(buffer);
            LOG.debug("Raw JSON received: {}", json);

            JsonRpcRequest request = jsonRpc.decodeRequest(json);
            id = request.id;

            if (!(client instanceof InetSocketAddress inetClient)) {
                LOG.warn("Unknown client address type: {}", client.getClass());
                return;
            }

            String ip = inetClient.getAddress().getHostAddress();
            int port = inetClient.getPort();

            RequestKey key = new RequestKey(id, ip, port);

            // Check cache for duplicates
            JsonRpcResponse<?> cached = cache.lookUp(key);
            if (cached != null) {
                LOG.info("Duplicate request detected, sending cached response for id={} to {}:{}", id, ip, port);
                transport.send(client, jsonRpc.encodeSuccess(cached.result, id));
                return;
            }

            LOG.info("Processing request id={} method={}", id, request.method);
            Object result = handler.handle(request);

            LOG.debug("Handler executed successfully for id={}", id);

            // Encode and send response
            byte[] response = jsonRpc.encodeSuccess(result, id);
            transport.send(client, response);

            // Store in cache for duplicate detection
            cache.add(key, new JsonRpcResponse<>() {{
                this.id = id;
                this.result = result;
            }});

            LOG.info("Response sent for id={} to {}:{}", id, ip, port);

        } catch (Exception e) {
            LOG.error("Error during packet processing (id={})", id, e);

            if (client != null) {
                try {
                    byte[] err = jsonRpc.encodeError(
                            JsonRpcError.INTERNAL_ERROR,
                            "Internal error",
                            id
                    );
                    transport.send(client, err);
                    LOG.info("Error response sent to {} for id={}", client, id);
                } catch (IOException ignored) {
                    LOG.error("Failed to send error response", ignored);
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