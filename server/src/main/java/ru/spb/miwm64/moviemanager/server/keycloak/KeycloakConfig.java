package ru.spb.miwm64.moviemanager.server.keycloak;

public class KeycloakConfig {
    public String baseUrl = "http://localhost:8989"; // host.docker.internal
    public String masterRealmName = "master";
    public String targetRealmName = "movie";

    public String adminClientId = "admin-cli";
    public String adminUsername = "miwm64";
    public String adminPassword = "12345";

    public String ClientId = "movie-api";
    public String ClientSecret = "WPfcmcDYln8pxtZE2sRl0f3MKmohGpNL";
}
