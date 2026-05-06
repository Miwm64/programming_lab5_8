package ru.spb.miwm64.moviemanager.server.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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


    private final JsonRpc jsonRpc;
    private final RequestRouter handler;

    private final CacheManager cache = new CacheManager();

    private static final Logger LOG = LoggerFactory.getLogger(PacketProcessor.class);

    public PacketProcessor(JsonRpc codec,
                           RequestRouter handler) {
        this.jsonRpc = codec;
        this.handler = handler;
        LOG.debug("PacketProcessor initialized");
    }

    public byte[] process(SocketAddress client, ByteBuffer buffer) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        try {
            LOG.debug("Processing packet from {}", client);

            String json = extract(buffer);
            LOG.debug("Raw JSON received: {}", json);

            JsonRpcRequest request = jsonRpc.decodeRequest(json);
            Integer id = request.id;
            UUID uuid = request.uuid;

            RequestKey key = new RequestKey(id, uuid);

            // Check cache for duplicates
            JsonRpcResponse<?> cached = cache.lookUp(key);
            if (cached != null) {
                LOG.info("Duplicate request detected, sending cached response for id={} to {}", id, uuid);
                return jsonRpc.encodeSuccess(cached.result, id, uuid);
            }

            LOG.info("Processing request id={} method={}", id, request.method);

            // This will be synchronized by the caller
            Object result = handler.route(request.method, request.params);

            // Store in cache
            JsonRpcResponse<Object> response = new JsonRpcResponse<>();
            response.id = id;
            response.result = result;
            cache.add(key, response);
            LOG.info("Added response to cache with uuid={} id={}", uuid, id);

            return jsonRpc.encodeSuccess(result, id, uuid);

        } catch (Exception e) {
            LOG.error("Error during packet processing", e);

            try {
                return jsonRpc.encodeError(
                        JsonRpcError.INTERNAL_ERROR,
                        "Internal error: " + e.getMessage(),
                        null,
                        null
                );
            } catch (Exception encodeError) {
                LOG.error("Failed to encode error response", encodeError);
                return null;
            }
        } finally {
            MDC.clear();
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