package ru.spb.miwm64.moviemanager.server.net;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.spb.miwm64.moviemanager.common.exceptions.NonExistentCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RequestRouter {
    private final Map<String, Handler> handlers = new HashMap<>();
    private final CollectionManager collectionManager;
    private final ObjectMapper mapper;

    private static final Logger LOG = LoggerFactory.getLogger(RequestRouter.class);

    public RequestRouter(CollectionManager collectionManager, ObjectMapper mapper) {
        this.collectionManager = collectionManager;
        this.mapper = mapper;
        LOG.debug("Initializing RequestRouter");
        registerHandlers();
        LOG.info("RequestRouter initialized with {} handlers", handlers.size());
    }

    private void registerHandlers() {
        LOG.debug("Registering handlers");

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

        LOG.debug("Handlers registered: {}", handlers.keySet());
    }

    public Object route(String method, JsonNode params) throws Exception {
        Handler handler = handlers.get(method);
        if (handler == null) {
            LOG.error("Unknown method requested: {}", method);
            throw new NonExistentCommand("Unknown method: " + method);
        }

        try {
            Object result = handler.handle(params);
            LOG.debug("Method executed successfully: {}", method);
            return result;
        } catch (Exception e) {
            LOG.error("Error while executing method: {}", method, e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface Handler {
        Object handle(JsonNode params) throws Exception;
    }
}