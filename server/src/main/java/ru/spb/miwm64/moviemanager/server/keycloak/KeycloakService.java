package ru.spb.miwm64.moviemanager.server.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.spb.miwm64.moviemanager.common.exceptions.WrongCredentials;

import java.util.HashMap;
import java.util.Map;

public class KeycloakService implements UserAuthService {
    private final KeycloakConfig config;
    private final KeycloakHttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public KeycloakService(KeycloakConfig config) {
        this.config = config;
        this.http = new KeycloakHttpClient(config.baseUrl);
    }

    // ---------- Helper: get admin token ----------
    private String getAdminToken() {
        String path = String.format("/realms/%s/protocol/openid-connect/token", config.masterRealmName);
        String body = String.format(
                "client_id=%s&grant_type=password&username=%s&password=%s",
                config.adminClientId, config.adminUsername, config.adminPassword
        );
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        String response = http.post(path, body, headers);
        try {
            JsonNode root = mapper.readTree(response);
            return root.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse admin token response", e);
        }
    }

    // ---------- Create User ----------
    @Override
    public String createUser(UserInfo userInfo, String password) {
        String adminToken = getAdminToken();
        String path = String.format("/admin/realms/%s/users", config.targetRealmName);

        // Build JSON with Jackson
        ObjectNode userJson = mapper.createObjectNode();
        userJson.put("username", userInfo.username);
        userJson.put("email", userInfo.email);
        userJson.put("firstName", userInfo.firstName);
        userJson.put("lastName", userInfo.lastName);
        userJson.put("enabled", true);
        ObjectNode cred = mapper.createObjectNode();
        cred.put("type", "password");
        cred.put("value", password);
        cred.put("temporary", false);
        userJson.set("credentials", mapper.createArrayNode().add(cred));

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + adminToken);
        http.post(path, userJson.toString(), headers);

        // Fetch the created user's ID by username
        return getUserIdByUsername(userInfo.username);
    }

    public String getUserIdByUsername(String username) {
        try {
            // First, get an admin token using client credentials
            String adminToken = getAdminToken();

            String path = String.format("/admin/realms/%s/users?username=%s&exact=true", config.targetRealmName, username);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + adminToken);
            String response = http.get(path, headers);
            JsonNode users = mapper.readTree(response);
            if (users.isArray() && users.size() > 0) {
                return users.get(0).path("id").asText();
            }
            throw new IllegalArgumentException("User not found: " + username);
        }
        catch (Exception e) {
            throw new RuntimeException("Error while executing method", e);
        }
    }

    // ---------- Delete User ----------
    @Override
    public boolean deleteUser(String userId, String token) {
        String adminToken = getAdminToken();
        String path = String.format("/admin/realms/%s/users/%s", config.targetRealmName, userId);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + adminToken);
        http.delete(path, headers);
        return true;
    }

    // ---------- Update User Info ----------
    @Override
    public void updateUserInfo(UserInfo userInfo, String token) {
        String adminToken = getAdminToken();
        // First fetch existing user to get full representation (PUT replaces all)
        String getPath = String.format("/admin/realms/%s/users/%s", config.targetRealmName, userInfo.userId);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + adminToken);
        String existingJson = http.get(getPath, headers);
        try {
            JsonNode existing = mapper.readTree(existingJson);
            ObjectNode update = existing.deepCopy();
            if (userInfo.username != null) update.put("username", userInfo.username);
            if (userInfo.email != null) update.put("email", userInfo.email);
            if (userInfo.firstName != null) update.put("firstName", userInfo.firstName);
            if (userInfo.lastName != null) update.put("lastName", userInfo.lastName);
            // Keep existing enabled flag unless you want to change it
            String putPath = String.format("/admin/realms/%s/users/%s", config.targetRealmName, userInfo.userId);
            http.put(putPath, update.toString(), headers);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    // ---------- Get User Info ----------
    @Override
    public UserInfo getUserInfo(UserInfo userInfo) {
        String adminToken = getAdminToken();
        String path;
        if (userInfo.userId != null && !userInfo.userId.isEmpty()) {
            path = String.format("/admin/realms/%s/users/%s", config.targetRealmName, userInfo.userId);
        } else {
            path = String.format("/admin/realms/%s/users?username=%s&exact=true", config.targetRealmName, userInfo.username);
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + adminToken);
        String response = http.get(path, headers);
        try {
            JsonNode node;
            if (userInfo.userId != null && !userInfo.userId.isEmpty()) {
                node = mapper.readTree(response);
            } else {
                JsonNode arr = mapper.readTree(response);
                if (arr.isArray() && arr.size() > 0) {
                    node = arr.get(0);
                } else {
                    throw new RuntimeException("User not found: " + userInfo.username);
                }
            }
            UserInfo result = new UserInfo();
            result.userId = node.path("id").asText(null);
            result.username = node.path("username").asText(null);
            result.email = node.path("email").asText(null);
            result.firstName = node.path("firstName").asText(null);
            result.lastName = node.path("lastName").asText(null);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user info", e);
        }
    }

    // ---------- Login ----------
    @Override
    public String login(String username, String password) {
        String path = String.format("/realms/%s/protocol/openid-connect/token", config.targetRealmName);
        String body = String.format(
                "client_id=%s&client_secret=%s&grant_type=password&username=%s&password=%s",
                config.clientId, config.clientSecret, username, password
        );
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String response = http.post(path, body, headers);
        try {
            JsonNode root = mapper.readTree(response);
            if (root.has("error")) {
                String error = root.get("error").asText();
                if ("invalid_grant".equals(error)) {
                    throw new WrongCredentials();
                }
                throw new RuntimeException("Keycloak error: " + error);
            }
            return root.get("access_token").asText();
        } catch (WrongCredentials e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse login response", e);
        }
    }

    // ---------- Validate Token ----------
    @Override
    public boolean validateToken(String token) {
        String path = String.format("/realms/%s/protocol/openid-connect/token/introspect", config.targetRealmName);
        String body = String.format(
                "client_id=%s&client_secret=%s&token=%s",
                config.clientId, config.clientSecret, token
        );
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        String response = http.post(path, body, headers);
        try {
            JsonNode root = mapper.readTree(response);
            return root.path("active").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUserIdFromToken(String token) {
        String path = String.format("/realms/%s/protocol/openid-connect/token/introspect", config.targetRealmName);
        String body = String.format(
                "client_id=%s&client_secret=%s&token=%s",
                config.clientId, config.clientSecret, token
        );
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        try {
            String response = http.post(path, body, headers);
            JsonNode root = mapper.readTree(response);
            boolean active = root.path("active").asBoolean(false);
            if (active) {
                JsonNode subNode = root.path("sub");
                if (!subNode.isMissingNode() && !subNode.isNull()) {
                    return subNode.asText();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}