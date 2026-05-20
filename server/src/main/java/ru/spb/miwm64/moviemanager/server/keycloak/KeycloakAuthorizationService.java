package ru.spb.miwm64.moviemanager.server.keycloak;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeycloakAuthorizationService {

    private final KeycloakHttpClient client;
    private final String realm;

    public KeycloakAuthorizationService(
            KeycloakHttpClient client,
            String realm
    ) {
        this.client = client;
        this.realm = realm;
    }


    public String createResource(String adminToken, String jsonBody) {

        return client.post(
                "/realms/" + realm + "/authz/protection/resource_set",
                jsonBody,
                authJson(adminToken)
        );
    }

    public void deleteResource(String adminToken, String resourceId) {

        client.delete(
                "/realms/" + realm + "/authz/protection/resource_set/" + resourceId,
                auth(adminToken)
        );
    }


    public String createGroup(String adminToken, String groupName) {

        String body = "{\"name\":\"" + groupName + "\"}";

        return client.post(
                "/admin/realms/" + realm + "/groups",
                body,
                authJson(adminToken)
        );
    }

    public String getGroupIdByName(String adminToken, String groupName) {

        String response = client.get(
                "/admin/realms/" + realm + "/groups",
                auth(adminToken)
        );

        // VERY SIMPLE parsing (Keycloak returns JSON array)
        String marker = "\"name\":\"" + groupName + "\"";

        int idx = response.indexOf(marker);
        if (idx == -1) {
            throw new RuntimeException("Group not found: " + groupName);
        }

        String idMarker = "\"id\":\"";
        int idStart = response.lastIndexOf(idMarker, idx);

        if (idStart == -1) {
            throw new RuntimeException("Group id not found");
        }

        idStart += idMarker.length();
        int idEnd = response.indexOf("\"", idStart);

        return response.substring(idStart, idEnd);
    }

    public void addUserToGroup(
            String adminToken,
            String userId,
            String groupId
    ) {

        client.put(
                "/admin/realms/" + realm + "/users/" + userId + "/groups/" + groupId,
                null,
                auth(adminToken)
        );
    }

    public void removeUserFromGroup(
            String adminToken,
            String userId,
            String groupId
    ) {

        client.delete(
                "/admin/realms/" + realm + "/users/" + userId + "/groups/" + groupId,
                auth(adminToken)
        );
    }


    public String createGroupPolicy(
            String adminToken,
            String name,
            String groupPath
    ) {

        String body =
                "{"
                        + "\"name\":\"" + name + "\","
                        + "\"groups\":[\"" + groupPath + "\"]"
                        + "}";

        return client.post(
                "/realms/" + realm + "/authz/resource-server/policy/group",
                body,
                authJson(adminToken)
        );
    }

    public String createScopePermission(
            String adminToken,
            String name,
            String resource,
            List<String> scopes,
            List<String> policies
    ) {

        String body =
                "{"
                        + "\"name\":\"" + name + "\","
                        + "\"resources\":[\"" + resource + "\"],"
                        + "\"scopes\":["
                        + join(scopes)
                        + "],"
                        + "\"policies\":["
                        + join(policies)
                        + "]"
                        + "}";

        return client.post(
                "/realms/" + realm + "/authz/resource-server/permission/scope",
                body,
                authJson(adminToken)
        );
    }


    public String checkPermission(
            String clientId,
            String clientSecret,
            String userAccessToken,
            String resource,
            String scope
    ) {

        String body =
                "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket"
                        + "&client_id=" + clientId
                        + "&client_secret=" + clientSecret
                        + "&audience=" + clientId
                        + "&permission=" + resource + "#" + scope
                        + "&response_mode=decision";

        return client.post(
                "/realms/" + realm + "/protocol/openid-connect/token",
                body,
                formHeaders(userAccessToken)
        );
    }

    private Map<String, String> auth(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }

    private Map<String, String> authJson(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private Map<String, String> formHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }

    private String join(List<String> items) {

        if (items == null || items.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {

            sb.append("\"").append(items.get(i)).append("\"");

            if (i < items.size() - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }
}