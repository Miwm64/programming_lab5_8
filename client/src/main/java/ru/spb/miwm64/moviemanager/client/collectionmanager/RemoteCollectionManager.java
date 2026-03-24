package ru.spb.miwm64.moviemanager.client.collectionmanager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.spb.miwm64.moviemanager.client.entities.Movie;
import ru.spb.miwm64.moviemanager.client.entities.Person;
import ru.spb.miwm64.moviemanager.client.exceptions.SerializationException;
import ru.spb.miwm64.moviemanager.client.exceptions.WrongPacketException;
import ru.spb.miwm64.moviemanager.client.net.ConnectionClient;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcResponse;

import java.util.ArrayList;
import java.util.Objects;

// TODO Check if resp.id == req.id

public class RemoteCollectionManager implements CollectionManager {
    private final JsonRpcClient jsonRpcClient;

    public RemoteCollectionManager(JsonRpcClient jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;
    }

    @Override
    public int append(Movie movie) {
        return jsonRpcClient.call("add", movie, Integer.class);
    }

    @Override
    public boolean addIfMin(Movie movie) {
        return false;
    }

    @Override
    public void setCollection(ArrayList<Movie> movies) {

    }

    @Override
    public void setById(Long id, Movie movie) {

    }

    @Override
    public Movie getById(Long id) {
        return null;
    }

    @Override
    public Movie getByIndex(int index) {
        return null;
    }

    @Override
    public ArrayList<Movie> getGreater(Person person) {
        return null;
    }

    @Override
    public ArrayList<Movie> getAll() {
        return null;
    }

    @Override
    public void removeById(Long id) {

    }

    @Override
    public void removeByIndex(int index) {

    }

    @Override
    public void removeGreater(Movie movie) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void clear() {

    }

    @Override
    public long countByGoldenPalmCount(long count) {
        return 0;
    }

    @Override
    public ArrayList<Movie> filterGreaterThanOperatorCommand(Person p) {
        return null;
    }

    @Override
    public ArrayList<Movie> printFieldAscendingGoldenPalmCountCommand() {
        return null;
    }
}
