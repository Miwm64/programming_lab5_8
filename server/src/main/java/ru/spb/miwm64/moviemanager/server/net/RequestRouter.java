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
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchStreamCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.DbBatchCollectionManager;
import ru.spb.miwm64.moviemanager.server.db.SQLRepository;
import ru.spb.miwm64.moviemanager.server.keycloak.KeycloakConfig;
import ru.spb.miwm64.moviemanager.server.keycloak.KeycloakService;
import ru.spb.miwm64.moviemanager.server.keycloak.UserAuthService;
import ru.spb.miwm64.moviemanager.server.keycloak.UserInfo;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

public class RequestRouter {
    private final Map<String, Handler> handlers = new HashMap<>();
    private final DbBatchCollectionManager collectionManager;
    private final ObjectMapper mapper;
    private final SQLRepository sqlRepository;
    private final UserAuthService userAuthService;

    private static final Logger LOG = LoggerFactory.getLogger(RequestRouter.class);

    public RequestRouter(DbBatchCollectionManager collectionManager,
                         ObjectMapper mapper, SQLRepository sqlRepository, UserAuthService userAuthService
                         ) {
        this.sqlRepository = sqlRepository;
        this.collectionManager = collectionManager;
        this.mapper = mapper;
        this.userAuthService = userAuthService;
        LOG.debug("Initializing RequestRouter");
        registerHandlers();
        LOG.info("RequestRouter initialized with {} handlers", handlers.size());
    }

    private void registerHandlers() {
        LOG.debug("Registering handlers");
        handlers.put("register", (JsonNode params, String token) -> {
            String username = params.get("username").asText();
            String password = params.get("password").asText();
            String email = params.get("email").asText();
            String firstName = params.get("firstName").asText();
            String lastName = params.get("lastName").asText();

            UserInfo userInfo = new UserInfo();
            userInfo.userId = null;
            userInfo.username = username;
            userInfo.email = email;
            userInfo.firstName = firstName;
            userInfo.lastName = lastName;

            return userAuthService.createUser(userInfo, password);
        });
        handlers.put("login", (JsonNode params, String token) -> {
            String userName = params.get("username").asText();
            String password = params.get("password").asText();
            return userAuthService.login(userName, password);
        });

        handlers.put("deleteUser", (JsonNode params, String token) -> {
            return userAuthService.deleteUser(userAuthService.getUserIdFromToken(token), token);
        });

        handlers.put("grantAccess", (JsonNode params, String token) -> {
            long movieId = params.get("movieId").asLong();
            String targetUsername = params.get("nickname").asText();

            String requesterId = userAuthService.getUserIdFromToken(token);
            if (!sqlRepository.isOwner(movieId, requesterId)) {
                throw new RuntimeException("Only the movie owner can grant access");
            }

            String targetUserId = userAuthService.getUserIdByUsername(targetUsername);
            sqlRepository.grantAccess(movieId, targetUserId, "edit");

            return true;
        });

        handlers.put("revokeAccess", (JsonNode params, String token) -> {
            long movieId = params.get("movieId").asLong();
            String targetUsername = params.get("nickname").asText();

            String requesterId = userAuthService.getUserIdFromToken(token);
            if (!sqlRepository.isOwner(movieId, requesterId)) {
                throw new RuntimeException("Only the movie owner can revoke access");
            }

            String targetUserId = userAuthService.getUserIdByUsername(targetUsername);
            sqlRepository.revokeAccess(movieId, targetUserId);

            return true;
        });

        handlers.put("sync", (JsonNode params, String token) -> {
            JsonNode pendingNode = params.get("pendingBatch");
            Batch pendingBatch = (pendingNode == null || pendingNode.isNull())
                    ? null
                    : mapper.treeToValue(pendingNode, Batch.class);

            JsonNode versionsNode = params.get("clientVersions");
            Map<Long, Integer> clientVersions = new HashMap<>();
            if (versionsNode != null && !versionsNode.isNull()) {
                Iterator<Map.Entry<String, JsonNode>> fields = versionsNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    clientVersions.put(Long.parseLong(entry.getKey()), entry.getValue().asInt());
                }
            }
            return collectionManager.applyBatch(pendingBatch, clientVersions,
                    userAuthService.getUserIdFromToken(token));
        });

        LOG.debug("Handlers registered: {}", handlers.keySet());
    }

    public Object route(String method, JsonNode params, String token) throws Exception {
        boolean isTokenValid = userAuthService.validateToken(token);
        if (!isTokenValid && !method.equals("register") && !method.equals("login")) {
            throw new RuntimeException("Invalid token");
        }
        Handler handler = handlers.get(method);
        if (handler == null) {
            LOG.error("Unknown method requested: {}", method);
            throw new NonExistentCommand("Unknown method: " + method);
        }

        try {
            Object result = handler.handle(params, token);
            LOG.debug("Method executed successfully: {}", method);
            return result;
        } catch (Exception e) {
            LOG.error("Error while executing method: {}", method, e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface Handler {
        Object handle(JsonNode params, String token) throws Exception;
    }
}