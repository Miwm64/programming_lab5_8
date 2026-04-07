package ru.spb.miwm64.moviemanager.client.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.spb.miwm64.moviemanager.client.exceptions.*;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcError;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcResponse;
import ru.spb.miwm64.moviemanager.common.exceptions.InvalidValueException;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class JsonRpcClient {
    private static final Logger LOG = LoggerFactory.getLogger(JsonRpcClient.class);

    private final static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final ConnectionClient connection;
    private Integer nextId = 1;
    private final UUID uuid;

    public JsonRpcClient(ConnectionClient connection) {
        this.connection = connection;
        LOG.info("JsonRpcClient initialized");
        this.uuid = UUID.randomUUID();
    }

    public <T> T call(String method, Object params, TypeReference<T> resultType)
            throws NetException, InvalidValueException, NoSuchElementException {

        Integer id = nextId+1;
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        try {
            LOG.info("Sending JSON-RPC request '{}' id={} params={}", method, id, params);

            JsonRpcRequest request = new JsonRpcRequest(id, method, objectMapper.valueToTree(params), uuid);
            String requestJson = objectMapper.writeValueAsString(request);
            String responseJson = connection.exchangeString(requestJson);

            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(JsonRpcResponse.class,
                            objectMapper.getTypeFactory().constructType(resultType));

            JsonRpcResponse<T> response = objectMapper.readValue(responseJson, type);
            ++nextId;

            if (!Objects.equals(request.id, response.id)) {
                throw new WrongPacketException();
            }
            if (!Objects.equals(request.uuid, response.uuid)) {
                throw new WrongPacketException();
            }

            if (response.error != null) {
                LOG.error("JSON-RPC call '{}' failed with error: {}", method, response.error.message);
                throw mapToCollectionException(response.error);
            }

            return response.result;
        } catch (NetException e) {
            LOG.error("Network error during JSON-RPC call '{}'", method, e);
            throw e;
        } catch (IllegalArgumentException e) {
            LOG.error("Serialization error during JSON-RPC call '{}'", method, e);
            throw new SerializationException(e);
        } catch (Exception e) {
            LOG.error("Unexpected error during JSON-RPC call '{}'", method, e);
            throw new NetException("JSON-RPC error: " + e.getMessage(), e);
        } finally {
            MDC.remove("requestId");
        }
    }

    private RuntimeException mapToCollectionException(JsonRpcError error) {
        return switch (error.code) {
            case JsonRpcError.INVALID_VALUE -> new InvalidValueException(error.message);
            case JsonRpcError.NOT_FOUND -> new NoSuchElementException(error.message);
            default -> new RuntimeException(error.message);
        };
    }
}