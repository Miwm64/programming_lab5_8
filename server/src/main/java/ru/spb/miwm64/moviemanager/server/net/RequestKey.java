package ru.spb.miwm64.moviemanager.server.net;

import java.util.UUID;

public record RequestKey(int requestId, UUID uuid) {}