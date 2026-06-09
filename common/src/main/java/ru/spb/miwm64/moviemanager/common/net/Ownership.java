package ru.spb.miwm64.moviemanager.common.net;

public record Ownership(String userId, Long movieId, OwnershipType type) {}
