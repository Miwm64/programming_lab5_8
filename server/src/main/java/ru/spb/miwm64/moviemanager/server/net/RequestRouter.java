package ru.spb.miwm64.moviemanager.server.net;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.spb.miwm64.moviemanager.common.exceptions.NonExistentCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.net.Batch;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchStreamCollectionManager;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

public class RequestRouter {
    private final Map<String, Handler> handlers = new HashMap<>();
    private final BatchStreamCollectionManager collectionManager;
    private final ObjectMapper mapper;

    private static final Logger LOG = LoggerFactory.getLogger(RequestRouter.class);

    public RequestRouter(BatchStreamCollectionManager collectionManager, ObjectMapper mapper) {
        this.collectionManager = collectionManager;
        this.mapper = mapper;
        LOG.debug("Initializing RequestRouter");
        registerHandlers();
        LOG.info("RequestRouter initialized with {} handlers", handlers.size());
    }

    private void registerHandlers() {
        LOG.debug("Registering handlers");
        handlers.put("sync", params -> {
            Batch batch = mapper.treeToValue(params, Batch.class);
            return collectionManager.applyBatch(batch);
        });

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