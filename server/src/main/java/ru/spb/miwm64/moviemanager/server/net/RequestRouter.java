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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

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
        handlers.put("sync", params -> {
            // Create operator Person objects
            Person tarantino = new Person("Quentin Tarantino", 85, Color.BROWN, Country.INDIA);
            Person gerwig = new Person("Greta Gerwig", 85, Color.BROWN, Country.INDIA);
            Person bong = new Person("Bong Joon-ho", 85, Color.BROWN, Country.INDIA);

            // Create movies
            Movie updatedMovie = new Movie(1L, "Updated Movie Title", new Coordinates(1, 2l),
                    ZonedDateTime.now(), 1, 1, MovieGenre.DRAMA, MpaaRating.R, tarantino);
            Movie newMovieA = new Movie(2L, "New Movie A", new Coordinates(1, 2l),
                    ZonedDateTime.now(), 1, 1, MovieGenre.DRAMA, MpaaRating.R, gerwig);
            Movie newMovieB = new Movie(3L, "New Movie B", new Coordinates(1, 2l),
                    ZonedDateTime.now(), 1, 1, MovieGenre.DRAMA, MpaaRating.R, bong);

            // Wrap in VersionedObject with version numbers
            VersionedObject<Movie> updateVO = new VersionedObject<>(5, updatedMovie);
            VersionedObject<Movie> createVO_A = new VersionedObject<>(1, newMovieA);
            VersionedObject<Movie> createVO_B = new VersionedObject<>(1, newMovieB);

// Build the lists
            ArrayList<VersionedObject<Movie>> updates = new ArrayList<>(Collections.singletonList(updateVO));
            ArrayList<VersionedObject<Movie>> creates = new ArrayList<>(Arrays.asList(createVO_A, createVO_B));
            ArrayList<Long> deletes = new ArrayList<>(Collections.singletonList(4L));
            ArrayList<String> messages = new ArrayList<>(Arrays.asList(
                    "Update for movie 1 applied successfully",
                    "Movie 2 created",
                    "Movie 3 created",
                    "Movie 4 deleted"
            ));

// Return the Batch object
            return new Batch(updates, creates, deletes, messages);
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