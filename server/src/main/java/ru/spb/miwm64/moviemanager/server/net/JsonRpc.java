package ru.spb.miwm64.moviemanager.server.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcError;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonRpc {
    private final ObjectMapper objectMapper;

    private static final Logger LOG = LoggerFactory.getLogger(JsonRpc.class);

    public JsonRpc() {
        LOG.debug("Initializing JsonRpc ObjectMapper");

        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        LOG.info("JsonRpc initialized");
    }

    public JsonRpcRequest decodeRequest(String json) throws IOException {
        try {
            JsonRpcRequest request = objectMapper.readValue(json, JsonRpcRequest.class);
            LOG.debug("Decoded request: id={}, method={}", request.id, request.method);
            return request;
        } catch (IOException e) {
            LOG.error("Failed to decode JSON-RPC request", e);
            throw e;
        }
    }

    public byte[] encodeSuccess(Object result, int id) throws IOException {
        try {
            JsonRpcResponse<Object> res = new JsonRpcResponse<>();
            res.id = id;
            res.result = result;

            byte[] bytes = objectMapper.writeValueAsBytes(res);
            LOG.debug("Encoded success response ({} bytes) for id={}", bytes.length, id);
            return bytes;
        } catch (IOException e) {
            LOG.error("Failed to encode success response for id={}", id, e);
            throw e;
        }
    }

    public byte[] encodeError(int code, String message, Integer id) throws IOException {
        LOG.error("Encoding error response for id={}, code={}, message={}", id, code, message);
        try {
            JsonRpcResponse<Void> res = new JsonRpcResponse<>();
            res.id = id;
            res.error = new JsonRpcError(code, message, null);

            byte[] bytes = objectMapper.writeValueAsBytes(res);
            LOG.debug("Encoded error response ({} bytes) for id={}", bytes.length, id);
            return bytes;
        } catch (IOException e) {
            LOG.error("Failed to encode error response for id={}", id, e);
            throw e;
        }
    }
}