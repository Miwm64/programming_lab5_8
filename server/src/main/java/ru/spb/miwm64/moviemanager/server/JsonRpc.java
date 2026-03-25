package ru.spb.miwm64.moviemanager.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcError;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcResponse;

import java.io.IOException;

public class JsonRpc {
    private final ObjectMapper objectMapper;

    public JsonRpc() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public JsonRpcRequest decodeRequest(String json) throws IOException {
        return objectMapper.readValue(json, JsonRpcRequest.class);
    }

    public byte[] encodeSuccess(Object result, int id) throws IOException {
        JsonRpcResponse<Object> res = new JsonRpcResponse<>();
        res.id = id;
        res.result = result;
        return objectMapper.writeValueAsBytes(res);
    }

    public byte[] encodeError(int code, String message, Integer id) throws IOException {
        JsonRpcResponse<Void> res = new JsonRpcResponse<>();
        res.id = id;
        res.error = new JsonRpcError(code, message, null);
        return objectMapper.writeValueAsBytes(res);
    }
}
