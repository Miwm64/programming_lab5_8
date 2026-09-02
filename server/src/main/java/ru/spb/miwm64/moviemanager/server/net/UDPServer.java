package ru.spb.miwm64.moviemanager.server.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.io.Reader;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.server.Main;
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.DbBatchCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.LoadManager;
import ru.spb.miwm64.moviemanager.server.db.SQLRepository;
import ru.spb.miwm64.moviemanager.server.io.NonBlockingConsoleReader;
import ru.spb.miwm64.moviemanager.server.keycloak.UserAuthService;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPServer {
    private final UDPTransport transport;
    private final PacketProcessor processor;
    private final LoadManager loadManager;
    private final Reader reader;
    private final DbBatchCollectionManager collectionManager;

    private static final Logger mainLOG = LoggerFactory.getLogger(Main.class);
    private static final Logger LOG = LoggerFactory.getLogger(UDPServer.class);

    private final AtomicBoolean running = new AtomicBoolean(true);

    // 3 thread pools
    private final ExecutorService readPool;
    private final ExecutorService processPool;
    private final ExecutorService writePool;

    // queues between thread pools
    private final BlockingQueue<ReceivedPacket> readQueue;
    private final BlockingQueue<ProcessedPacket> processQueue;

    private static final int MAX_PACKET_SIZE = 65536;
    private static final int READ_POOL_SIZE = 4;
    private static final int PROCESS_POOL_SIZE = 8;
    private static final int WRITE_POOL_SIZE = 4;

    public UDPServer(int port, DbBatchCollectionManager collectionManager, XMLParser xmlParser,
                     SQLRepository sqlRepository, UserAuthService userAuthService) throws IOException {
        LOG.debug("Initializing UDPServer on port {}", port);

        this.transport = new UDPTransport(port);
        LOG.debug("UDP transport created");

        // Initialize JSON-RPC components
        JsonRpc jsonRpc = new JsonRpc();
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        RequestRouter router = new RequestRouter(collectionManager, objectMapper, sqlRepository, userAuthService);

        this.processor = new PacketProcessor(jsonRpc, router);
        this.collectionManager = collectionManager;
        this.loadManager = new LoadManager(collectionManager, xmlParser);
        this.reader = new NonBlockingConsoleReader();

        this.readPool = Executors.newVirtualThreadPerTaskExecutor();
        this.processPool = Executors.newVirtualThreadPerTaskExecutor();
        this.writePool = Executors.newVirtualThreadPerTaskExecutor();

        this.readQueue = new LinkedBlockingQueue<>();
        this.processQueue = new LinkedBlockingQueue<>();

        LOG.debug("Loading collection");
        loadManager.loadCollection();

        mainLOG.info("Server started");
        LOG.info("Server fully initialized with thread pools (read:{}, process:{}, write:{})",
                    READ_POOL_SIZE, PROCESS_POOL_SIZE, WRITE_POOL_SIZE);
    }

    public void run() {
        LOG.info("Server pipeline started");

        startReadStage();
        startProcessStage();
        startWriteStage();

        handleConsoleLoop();

        shutdown();
    }

    private void startReadStage() {
        LOG.info("Starting {} reader threads", READ_POOL_SIZE);
        for (int i = 0; i < READ_POOL_SIZE; i++) {
            readPool.submit(() -> {
                Thread.currentThread().setName("Reader-" + Thread.currentThread().getId());
                while (running.get()) {
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
                        SocketAddress client = transport.receive(buffer);

                        if (client != null && buffer.position() > 0) {
                            LOG.debug("Packet received from {}", client);
                            readQueue.put(new ReceivedPacket(client, buffer));
                        }
                    } catch (IOException e) {
                        if (running.get()) {
                            LOG.error("Error receiving packet", e);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                LOG.info("Reader thread stopped");
            });
        }
    }

    private void startProcessStage() {
        LOG.info("Starting {} processor threads", PROCESS_POOL_SIZE);
        for (int i = 0; i < PROCESS_POOL_SIZE; i++) {
            processPool.submit(() -> {
                Thread.currentThread().setName("Processor-" + Thread.currentThread().getId());
                while (running.get()) {
                    try {
                        ReceivedPacket packet = readQueue.take();
                        if (packet == null) continue;

                        LOG.debug("Processing packet from {}", packet.client);

                        byte[] response;
                        synchronized (collectionManager) {
                            response = processor.process(packet.client, packet.buffer);
                        }

                        if (response != null) {
                            processQueue.put(new ProcessedPacket(packet.client, response));
                            LOG.debug("Packet processed, queued for sending to {}", packet.client);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        LOG.error("Error in processing stage", e);
                    }
                }
                LOG.info("Processor thread stopped");
            });
        }
    }

    private void startWriteStage() {
        LOG.info("Starting {} writer threads", WRITE_POOL_SIZE);
        for (int i = 0; i < WRITE_POOL_SIZE; i++) {
            writePool.submit(() -> {
                Thread.currentThread().setName("Writer-" + Thread.currentThread().getId());
                while (running.get()) {
                    try {
                        ProcessedPacket packet = processQueue.take();
                        if (packet == null) continue;

                        transport.send(packet.client, packet.response);
                        LOG.debug("Response sent to {}", packet.client);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (IOException e) {
                        LOG.error("Failed to send response", e);
                    }
                }
                LOG.info("Writer thread stopped");
            });
        }
    }

    private void handleConsoleLoop() {
        LOG.info("Console handler started");
        while (running.get()) {
            try {
                String input = reader.readNextLine();
                if (input == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }

                LOG.debug("Console input received: {}", input);

                if ("exit".equalsIgnoreCase(input)) {
                    LOG.info("Exit command received");
                    stop();
                    break;
                }
                if ("load".equalsIgnoreCase(input)) {
                    LOG.info("Manual load triggered");
                    synchronized (collectionManager) {
                        loadManager.loadCollection();
                    }
                }
                if ("save".equalsIgnoreCase(input)) {
                    LOG.info("Manual save triggered");
                    synchronized (collectionManager) {
                        loadManager.saveCollection();
                    }
                }
            } catch (IOException e) {
                LOG.error("Error reading console input", e);
            }
        }
    }

    public void stop() {
        LOG.info("Stopping server");
        running.set(false);

        // Shutdown
        readPool.shutdown();
        processPool.shutdown();
        writePool.shutdown();
    }

    private void shutdown() {
        LOG.info("Shutting down server...");

        try {
            // Wait for pool shutdown
            if (!readPool.awaitTermination(5, TimeUnit.SECONDS)) {
                readPool.shutdownNow();
            }
            if (!processPool.awaitTermination(5, TimeUnit.SECONDS)) {
                processPool.shutdownNow();
            }
            if (!writePool.awaitTermination(5, TimeUnit.SECONDS)) {
                writePool.shutdownNow();
            }

            transport.close();
        } catch (InterruptedException e) {
            readPool.shutdownNow();
            processPool.shutdownNow();
            writePool.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            LOG.error("Error closing transport", e);
        }

        LOG.info("Saving collection before shutdown");
        synchronized (collectionManager) {
            loadManager.saveCollection();
        }
        LOG.info("Collection saved, server stopped");
    }


    private static class ReceivedPacket {
        final SocketAddress client;
        final ByteBuffer buffer;

        ReceivedPacket(SocketAddress client, ByteBuffer buffer) {
            this.client = client;
            this.buffer = buffer;
        }
    }

    private static class ProcessedPacket {
        final SocketAddress client;
        final byte[] response;

        ProcessedPacket(SocketAddress client, byte[] response) {
            this.client = client;
            this.response = response;
        }
    }
}