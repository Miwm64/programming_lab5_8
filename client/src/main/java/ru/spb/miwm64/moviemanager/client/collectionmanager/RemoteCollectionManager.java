package ru.spb.miwm64.moviemanager.client.collectionmanager;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.Person;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO Check if resp.id == req.id

public class RemoteCollectionManager implements CollectionManager {
    private final JsonRpcClient jsonRpcClient;

    public RemoteCollectionManager(JsonRpcClient jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;
    }

    @Override
    public int add(Movie movie) {
        return jsonRpcClient.call("add", movie, Integer.class);
    }

    @Override
    public boolean addIfMin(Movie movie) {
        return jsonRpcClient.call("addIfMin", movie, Boolean.class);
    }

    @Override
    public void setById(Long id, Movie movie) {
        jsonRpcClient.call("setById", Map.of("id", id, "movie", movie), Void.class);
    }

    @Override
    public Movie getById(Long id) {
        return jsonRpcClient.call("getById", Map.of("id", id), Movie.class);
    }

    @Override
    public Movie getByIndex(int index) {
        return jsonRpcClient.call("getByIndex", Map.of("index", index), Movie.class);
    }

    @Override
    public ArrayList<Movie> getGreater(Person person) {
        List<Movie> list = jsonRpcClient.call("getGreater", person, List.class);
        return new ArrayList<>(list);
    }

    @Override
    public ArrayList<Movie> getAll() {
        List<Movie> list = jsonRpcClient.call("getAll", null, List.class);
        return new ArrayList<>(list);
    }

    @Override
    public void removeById(Long id) {
        jsonRpcClient.call("removeById", Map.of("id", id), Void.class);
    }

    @Override
    public void removeByIndex(int index) {
        jsonRpcClient.call("removeByIndex", Map.of("index", index), Void.class);
    }

    @Override
    public void removeGreater(Movie movie) {
        jsonRpcClient.call("removeGreater", movie, Void.class);
    }

    @Override
    public void removeAll() {
        jsonRpcClient.call("removeAll", null, Void.class);
    }

    @Override
    public void clear() {
        jsonRpcClient.call("clear", null, Void.class);
    }

    @Override
    public long countByGoldenPalmCount(long count) {
        return jsonRpcClient.call("countByGoldenPalmCount", Map.of("count", count), Long.class);
    }

    @Override
    public ArrayList<Movie> filterGreaterThanOperatorCommand(Person p) {
        List<Movie> list = jsonRpcClient.call("filterGreaterThanOperator", p, List.class);
        return new ArrayList<>(list);
    }

    @Override
    public ArrayList<Movie> printFieldAscendingGoldenPalmCountCommand() {
        List<Movie> list = jsonRpcClient.call("printFieldAscendingGoldenPalmCount", null, List.class);
        return new ArrayList<>(list);
    }
}