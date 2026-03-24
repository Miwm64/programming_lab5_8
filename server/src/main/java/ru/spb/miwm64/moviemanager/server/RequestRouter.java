package ru.spb.miwm64.moviemanager.server;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.spb.miwm64.moviemanager.common.exceptions.NonExistentCommand;

import java.util.HashMap;
import java.util.Map;

public class RequestRouter {
    private final Map<String, Handler> handlers = new HashMap<>();
    private final CollectionManager collectionManager;
    private final ObjectMapper mapper;

    public RequestRouter(CollectionManager collectionManager, ObjectMapper mapper) {
        this.collectionManager = collectionManager;
        this.mapper = mapper;
        registerHandlers();
    }

    private void registerHandlers() {
        handlers.put("add", params -> collectionManager.add(mapper.treeToValue(params, Movie.class)));
        handlers.put("addIfMin", params -> collectionManager.addIfMin(mapper.treeToValue(params, Movie.class)));
        handlers.put("setById", params -> {
            long id = params.get("id").asLong();
            Movie movie = mapper.treeToValue(params.get("movie"), Movie.class);
            collectionManager.setById(id, movie);
            return null;
        });
        handlers.put("getById", params -> collectionManager.getById(params.get("id").asLong()));
        handlers.put("getByIndex", params -> collectionManager.getByIndex(params.get("index").asInt()));
        handlers.put("getGreater", params -> collectionManager.getGreater(mapper.treeToValue(params, Person.class)));
        handlers.put("getAll", params -> collectionManager.getAll());
        handlers.put("removeById", params -> {
            collectionManager.removeById(params.get("id").asLong());
            return null;
        });
        handlers.put("removeByIndex", params -> {
            collectionManager.removeByIndex(params.get("index").asInt());
            return null;
        });
        handlers.put("removeGreater", params -> {
            collectionManager.removeGreater(mapper.treeToValue(params, Movie.class));
            return null;
        });
        handlers.put("removeAll", params -> {
            collectionManager.removeAll();
            return null;
        });
        handlers.put("clear", params -> {
            collectionManager.clear();
            return null;
        });
        handlers.put("countByGoldenPalmCount", params ->
                collectionManager.countByGoldenPalmCount(params.get("count").asLong()));
        handlers.put("filterGreaterThanOperator", params ->
                collectionManager.filterGreaterThanOperatorCommand(mapper.treeToValue(params, Person.class)));
        handlers.put("printFieldAscendingGoldenPalmCount", params ->
                collectionManager.printFieldAscendingGoldenPalmCountCommand());
    }

    public Object route(String method, JsonNode params) throws Exception {
        Handler handler = handlers.get(method);
        if (handler == null) {
            throw new NonExistentCommand("Unknown method: " + method);
        }
        return handler.handle(params);
    }

    @FunctionalInterface
    public interface Handler {
        Object handle(JsonNode params) throws Exception;
    }
}