package ru.spb.miwm64.moviemanager.server.net;

import ru.spb.miwm64.moviemanager.common.net.JsonRpcResponse;

import java.util.HashMap;
import java.util.LinkedHashMap;

// LRU
public class CacheManager {
    private static final int MAX_SIZE = 1000;
    private final LinkedHashMap<RequestKey, JsonRpcResponse<?>> cache =
            new LinkedHashMap<>(16, 0.75f, true);;
    public CacheManager(){};

    public JsonRpcResponse<?> lookUp(RequestKey key) {
        return cache.get(key);
    }

    public void add(RequestKey key, JsonRpcResponse<?> response){
        if (cache.size() >= MAX_SIZE) {
            RequestKey oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey);
        }
        cache.put(key, response);
    }
}
