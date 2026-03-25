package ru.spb.miwm64.moviemanager.server.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.io.Reader;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.server.collectionmanager.LoadManager;
import ru.spb.miwm64.moviemanager.server.io.NonBlockingConsoleReader;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UDPServer {

    private final Selector selector;
    private final UDPTransport transport;
    private final PacketProcessor processor;
    private final LoadManager loadManager;
    private final Reader reader;

    private boolean running = true;

    public UDPServer(int port, CollectionManager collectionManager, XMLParser xmlParser) throws IOException {

        this.transport = new UDPTransport(port);
        this.selector = Selector.open();

        transport.getChannel().register(selector, SelectionKey.OP_READ);

        JsonRpc jsonrpc = new JsonRpc();
        RequestRouter router = new RequestRouter(collectionManager, new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL));
        RequestHandler handler = new RequestHandler(router);

        this.processor = new PacketProcessor(transport, jsonrpc, handler);
        this.loadManager = new LoadManager(collectionManager, xmlParser);
        this.reader = new NonBlockingConsoleReader();
        loadManager.loadCollection();
    }

    public void run() {
        while (running) {
            try {
                selector.select(100);

                for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isReadable()) {
                        processor.process();
                    }
                }

                handleConsole();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadManager.saveCollection();
    }

    private void handleConsole() throws IOException {
        String input = reader.readNextLine();
        if ("exit".equalsIgnoreCase(input)) {
            stop();
        }
        if ("load".equalsIgnoreCase(input)) {
            loadManager.loadCollection();
        }
        if ("save".equalsIgnoreCase(input)) {
            loadManager.saveCollection();
        }
    }

    public void stop() {
        running = false;
        try {
            selector.close();
            transport.close();
        } catch (IOException ignored) {}
    }
}