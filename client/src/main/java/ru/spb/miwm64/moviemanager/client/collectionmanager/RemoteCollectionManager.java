package ru.spb.miwm64.moviemanager.client.collectionmanager;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.Person;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class RemoteCollectionManager implements CollectionManager {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteCollectionManager.class);

    private final JsonRpcClient jsonRpcClient;

    public RemoteCollectionManager(JsonRpcClient jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;
        LOG.info("RemoteCollectionManager initialized");
    }

    @Override
    public int add(Movie movie) {
        return callRpc("add", movie, new TypeReference<Integer>() {});
    }

    @Override
    public boolean addIfMin(Movie movie) {
        return callRpc("addIfMin", movie, new TypeReference<Boolean>() {});
    }

    @Override
    public void setById(Long id, Movie movie) {
        callRpc("setById", Map.of("id", id, "movie", movie), new TypeReference<Void>() {});
    }

    @Override
    public Movie getById(Long id) {
        return callRpc("getById", Map.of("id", id), new TypeReference<Movie>() {});
    }

    @Override
    public Movie getByIndex(int index) {
        return callRpc("getByIndex", Map.of("index", index), new TypeReference<Movie>() {});
    }

    @Override
    public ArrayList<Movie> getGreater(Person person) {
        return callRpc("getGreater", person, new TypeReference<ArrayList<Movie>>() {});
    }

    @Override
    public ArrayList<Movie> getAll() {
        return callRpc("getAll", null, new TypeReference<ArrayList<Movie>>() {});
    }

    @Override
    public void removeById(Long id) {
        callRpc("removeById", Map.of("id", id), new TypeReference<Void>() {});
    }

    @Override
    public void removeByIndex(int index) {
        callRpc("removeByIndex", Map.of("index", index), new TypeReference<Void>() {});
    }

    @Override
    public void removeGreater(Movie movie) {
        callRpc("removeGreater", movie, new TypeReference<Void>() {});
    }

    @Override
    public void removeAll() {
        callRpc("removeAll", null, new TypeReference<Void>() {});
    }

    @Override
    public void clear() {
        callRpc("clear", null, new TypeReference<Void>() {});
    }

    @Override
    public long countByGoldenPalmCount(long count) {
        return callRpc("countByGoldenPalmCount", Map.of("count", count), new TypeReference<Long>() {});
    }

    @Override
    public ArrayList<Movie> filterGreaterThanOperatorCommand(Person p) {
        return callRpc("filterGreaterThanOperator", p, new TypeReference<ArrayList<Movie>>() {});
    }

    @Override
    public ArrayList<Movie> printFieldAscendingGoldenPalmCountCommand() {
        return callRpc("printFieldAscendingGoldenPalmCount", null, new TypeReference<ArrayList<Movie>>() {});
    }

    // --- Logging wrapper with MDC ---
    private <T> T callRpc(String method, Object params, TypeReference<T> type) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            LOG.info("Calling RPC method '{}'", method);
            T result = jsonRpcClient.call(method, params, type);
            LOG.info("RPC method '{}' completed successfully", method);
            return result;
        } catch (Exception e) {
            LOG.error("RPC method '{}' failed", method, e);
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }
}