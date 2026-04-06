package ru.spb.miwm64.moviemanager.client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ru.spb.miwm64.moviemanager.client.collectionmanager.BatchRemoteCollectionManager;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.common.io.Writer;
import ru.spb.miwm64.moviemanager.common.net.Batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SynchronizationThread extends Thread {
    private static final long NORMAL_SYNC_INTERVAL_MS = 15_000L;
    private static final long BASE_RETRY_MS = 1_000L;
    private static final long MAX_RETRY_MS = 64_000L;

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizationThread.class);
    private volatile boolean isRunning = true;
    private final JsonRpcClient jsonRpcClient;
    private final PendingChangeQueue pendingChangeQueue;
    private final List<String> messages;
    private final BatchRemoteCollectionManager collectionManager;

    public SynchronizationThread(JsonRpcClient jsonRpcClient, PendingChangeQueue pendingChangeQueue,
                                 BatchRemoteCollectionManager collectionManager, List<String> messages) {
        this.jsonRpcClient = jsonRpcClient;
        this.pendingChangeQueue = pendingChangeQueue;
        this.messages = messages;
        this.collectionManager = collectionManager;
    }

    @Override
    public void run() {
        long retryDelay = BASE_RETRY_MS;
        while (isRunning) {
            try {
                if (sync()) {
                    if (retryDelay != BASE_RETRY_MS){
                        messages.add("Connection restored");
                    }
                    retryDelay = BASE_RETRY_MS;
                    Thread.sleep(NORMAL_SYNC_INTERVAL_MS);
                } else {
                    Thread.sleep(retryDelay);
                    retryDelay = Math.min(retryDelay * 2, MAX_RETRY_MS);
                }
            } catch (InterruptedException e) {
                LOG.info("Sync thread interrupted");
            } catch (Exception e) {
                LOG.error("Unexpected error in sync loop", e);
            }
        }
    }

    private boolean sync() {
        LOG.info("Synchronization started");
        Batch localBatch = pendingChangeQueue.getBatch();
        try {
            Map<String, Object> syncRequest = new HashMap<>();
            syncRequest.put("pendingBatch", localBatch);
            syncRequest.put("clientVersions", collectionManager.getVersionMap());
            Batch serverBatch = callRpc("sync", syncRequest, new TypeReference<Batch>() {});

            LOG.info("Synchronization successful");

            if (localBatch != null) {
                pendingChangeQueue.removeFirstBatch();
            }

            if (serverBatch.messages != null && !serverBatch.messages.isEmpty()){
                StringBuilder message = new StringBuilder("Server refused some local actions:\n");
                for (var msg : serverBatch.messages){
                    message.append(message).append("\n");
                }
                messages.add(message.toString());
            }

            collectionManager.applyRemoteBatch(serverBatch);
            return true;
        } catch (Exception e) {
            LOG.error("Synchronization failed", e);
            try {
                messages.add("Synchronization failed: " + e.getMessage());
            } catch (Exception ignored) {}
            return false;
        }
    }

    public void gracefulShutdown() {
        isRunning = false;
        this.interrupt();
        try {
            this.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sync();
    }

    public void close(){
        isRunning = false;
        this.interrupt();
    }

    // --- Logging wrapper with MDC ---
    private <T> T callRpc(String method, Object params, TypeReference<T> type) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            LOG.info("Calling RPC method '{}'", method);
            T result = jsonRpcClient.call(method, params, type);
            LOG.info("RPC method '{}' completed successfully", method);
            return result;
        } catch (Exception e) {
            LOG.error("RPC method '{}' failed", method, e);
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }
}
