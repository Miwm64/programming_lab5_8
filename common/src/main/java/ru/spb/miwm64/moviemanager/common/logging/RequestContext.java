package ru.spb.miwm64.moviemanager.common.logging;

import org.slf4j.MDC;

import java.util.UUID;

public final class RequestContext {
    private static final String KEY = "requestId";

    private RequestContext() {}

    public static void init() {
        MDC.put(KEY, UUID.randomUUID().toString());
    }

    public static void clear() {
        MDC.clear();
    }
}