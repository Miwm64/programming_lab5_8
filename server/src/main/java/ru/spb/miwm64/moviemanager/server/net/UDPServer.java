package ru.spb.miwm64.moviemanager.server.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.io.Reader;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.server.Main;
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchStreamCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.LoadManager;
import ru.spb.miwm64.moviemanager.server.io.NonBlockingConsoleReader;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.UUID;

public class UDPServer {

    private final Selector selector;
    private final UDPTransport transport;
    private final PacketProcessor processor;
    private final LoadManager loadManager;
    private final Reader reader;

    private static final Logger mainLOG = LoggerFactory.getLogger(Main.class);
    private static final Logger LOG = LoggerFactory.getLogger(UDPServer.class);

    private boolean running = true;

    public UDPServer(int port, BatchStreamCollectionManager collectionManager, XMLParser xmlParser) throws IOException {

        LOG.debug("Initializing UDPServer on port {}", port);

        this.transport = new UDPTransport(port);
        LOG.debug("UDP transport created");

        this.selector = Selector.open();
        LOG.debug("Selector opened");

        transport.getChannel().register(selector, SelectionKey.OP_READ);
        LOG.debug("Channel registered for OP_READ");

        JsonRpc jsonrpc = new JsonRpc();
        RequestRouter router = new RequestRouter(collectionManager, new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL));
        LOG.debug("Request pipeline initialized");

        this.processor = new PacketProcessor(transport, jsonrpc, router);
        this.loadManager = new LoadManager(collectionManager, xmlParser);
        this.reader = new NonBlockingConsoleReader();

        LOG.debug("Loading collection");
        loadManager.loadCollection();

        mainLOG.info("Server started");
        LOG.info("Server fully initialized");
    }

    public void run() {
        LOG.info("Server event loop started");

        while (running) {
            try {
                selector.select(100);

                for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isReadable()) {
                        String requestId = UUID.randomUUID().toString();
                        MDC.put("requestId", requestId);

                        LOG.debug("Processing incoming packet");

                        try {
                            processor.process();
                            LOG.debug("Packet processed successfully");
                        } catch (Exception e) {
                            LOG.error("Packet processing failed", e);
                            throw e;
                        } finally {
                            MDC.clear();
                        }
                    }
                }

                handleConsole();

            } catch (Exception e) {
                mainLOG.error("Error: {}", e.getMessage());
                LOG.error("Unhandled exception in main loop", e);
            }
        }

        LOG.info("Saving collection before shutdown");
        loadManager.saveCollection();
        LOG.info("Collection saved, server stopped");
    }

    private void handleConsole() throws IOException {
        String input = reader.readNextLine();
        if (input == null){
            return;
        }

        LOG.debug("Console input received: {}", input);

        if ("exit".equalsIgnoreCase(input)) {
            LOG.info("Exit command received");
            stop();
        }
        if ("load".equalsIgnoreCase(input)) {
            LOG.info("Manual load triggered");
            loadManager.loadCollection();
        }
        if ("save".equalsIgnoreCase(input)) {
            LOG.info("Manual save triggered");
            loadManager.saveCollection();
        }
    }

    public void stop() {
        LOG.info("Stopping server");
        running = false;
        try {
            selector.close();
            transport.close();
            LOG.info("Resources closed");
        } catch (IOException e) {
            LOG.error("Error during shutdown", e);
        }
    }
}