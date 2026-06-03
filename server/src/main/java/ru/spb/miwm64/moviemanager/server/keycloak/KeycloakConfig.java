package ru.spb.miwm64.moviemanager.server.keycloak;

public class KeycloakConfig {
    public KeycloakConfig(String baseUrl, String adminUsername, String adminPassword, String clientSecret) {
        this.baseUrl = baseUrl;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.clientSecret = clientSecret;

    }
    public final String baseUrl;
    public final String masterRealmName = "master";
    public final String targetRealmName = "movie";

    public final String adminClientId = "admin-cli";
    public final String adminUsername;
    public final String adminPassword;

    public final String clientId = "movie-api";
    public final String clientSecret;
}
