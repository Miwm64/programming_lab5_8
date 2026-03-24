package ru.spb.miwm64.moviemanager.client.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.spb.miwm64.moviemanager.client.exceptions.*;
import ru.spb.miwm64.moviemanager.common.exceptions.InvalidValueException;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcError;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcResponse;

import java.util.NoSuchElementException;
import java.util.Objects;

public class JsonRpcClient {
    private final static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // ← YOU HAVE THIS
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private final ConnectionClient connection;
    private Integer nextId = 1;

    public JsonRpcClient(ConnectionClient connection) {
        this.connection = connection;
    }

    public <T> T call(String method, Object params, Class<T> resultType)
        throws NetException, RuntimeException {

        Integer id = nextId++;

        try {
            JsonRpcRequest request = new JsonRpcRequest(id, method, params);
            String requestJson = objectMapper.writeValueAsString(request);
            String responseJson = connection.exchangeString(requestJson);

            JsonRpcResponse response = objectMapper.readValue(responseJson, JsonRpcResponse.class);

//            if (!Objects.equals(request.id, response.id)){
//                throw new WrongPacketException();
//            }
            if (response.error != null) {
                throw mapToCollectionException(response.error);
            }
            if (response.result == null) {
                return null;
            }

            return objectMapper.convertValue(response.result, resultType);
        } catch (NetException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new SerializationException(e);
        }
        catch (Exception e) {
            throw new NetException("JSON-RPC error: " + e.getMessage(), e);
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