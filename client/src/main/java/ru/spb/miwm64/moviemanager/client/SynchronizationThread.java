package ru.spb.miwm64.moviemanager.client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.common.io.Writer;
import java.util.UUID;

public class SynchronizationThread extends Thread {
    private static final long NORMAL_SYNC_INTERVAL_MS = 15_000L;
    private static final long BASE_RETRY_MS = 1_000L;
    private static final long MAX_RETRY_MS = 64_000L;

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizationThread.class);
    private volatile boolean isRunning = true;
    private final JsonRpcClient jsonRpcClient;
    private final PendingChangeQueue pendingChangeQueue;
    private final Writer writer;

    public SynchronizationThread(JsonRpcClient jsonRpcClient, PendingChangeQueue pendingChangeQueue,
                                 Writer writer) {
        this.jsonRpcClient = jsonRpcClient;
        this.pendingChangeQueue = pendingChangeQueue;
        this.writer = writer;
    }

    @Override
    public void run() {
        long retryDelay = BASE_RETRY_MS;
        while (isRunning) {
            try {
                if (sync()) {
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
            Batch serverBatch = callRpc("sync", localBatch, new TypeReference<Batch>() {});
            writer.writeln("Successful synchronization");
            if (localBatch != null) {
                pendingChangeQueue.removeFirstBatch();
            }
            return true;
        } catch (Exception e) {
            LOG.error("Synchronization failed", e);
            try {
                writer.writeln("Synchronization failed: " + e.getMessage());
            } catch (Exception ignored) {
            }
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
