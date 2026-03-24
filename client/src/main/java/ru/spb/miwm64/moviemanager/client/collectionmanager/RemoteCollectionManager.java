package ru.spb.miwm64.moviemanager.client.collectionmanager;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.Person;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoteCollectionManager implements CollectionManager {
    private final JsonRpcClient jsonRpcClient;

    public RemoteCollectionManager(JsonRpcClient jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;
    }

    @Override
    public int add(Movie movie) {
        return jsonRpcClient.call("add", movie, new TypeReference<Integer>() {});
    }

    @Override
    public boolean addIfMin(Movie movie) {
        return jsonRpcClient.call("addIfMin", movie, new TypeReference<Boolean>() {});
    }

    @Override
    public void setById(Long id, Movie movie) {
        jsonRpcClient.call("setById", Map.of("id", id, "movie", movie), new TypeReference<Void>() {});
    }

    @Override
    public Movie getById(Long id) {
        return jsonRpcClient.call("getById", Map.of("id", id), new TypeReference<Movie>() {});
    }

    @Override
    public Movie getByIndex(int index) {
        return jsonRpcClient.call("getByIndex", Map.of("index", index), new TypeReference<Movie>() {});
    }

    @Override
    public ArrayList<Movie> getGreater(Person person) {
        ArrayList<Movie> list = jsonRpcClient.call("getGreater", person, new TypeReference<ArrayList<Movie>>() {});
        return list;
    }

    @Override
    public ArrayList<Movie> getAll() {
        return jsonRpcClient.call("getAll", null, new TypeReference<ArrayList<Movie>>() {});
    }

    @Override
    public void removeById(Long id) {
        jsonRpcClient.call("removeById", Map.of("id", id), new TypeReference<Void>() {});
    }

    @Override
    public void removeByIndex(int index) {
        jsonRpcClient.call("removeByIndex", Map.of("index", index), new TypeReference<Void>() {});
    }

    @Override
    public void removeGreater(Movie movie) {
        jsonRpcClient.call("removeGreater", movie, new TypeReference<Void>() {});
    }

    @Override
    public void removeAll() {
        jsonRpcClient.call("removeAll", null, new TypeReference<Void>() {});
    }

    @Override
    public void clear() {
        jsonRpcClient.call("clear", null, new TypeReference<Void>() {});
    }

    @Override
    public long countByGoldenPalmCount(long count) {
        return jsonRpcClient.call("countByGoldenPalmCount", Map.of("count", count), new TypeReference<Long>() {});
    }

    @Override
    public ArrayList<Movie> filterGreaterThanOperatorCommand(Person p) {
        return jsonRpcClient.call("filterGreaterThanOperator", p, new TypeReference<ArrayList<Movie>>() {});
    }

    @Override
    public ArrayList<Movie> printFieldAscendingGoldenPalmCountCommand() {
        return jsonRpcClient.call("printFieldAscendingGoldenPalmCount", null, new TypeReference<ArrayList<Movie>>() {});
    }
}