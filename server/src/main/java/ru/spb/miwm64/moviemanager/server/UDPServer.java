package ru.spb.miwm64.moviemanager.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.io.Reader;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcError;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class UDPServer {

    private final Selector selector;
    private final UDPTransport transport;
    private final PacketProcessor processor;
    private final Reader reader;

    private boolean running = true;

    public UDPServer(int port, CollectionManager cm) throws IOException {

        this.transport = new UDPTransport(port);
        this.selector = Selector.open();

        transport.getChannel().register(selector, SelectionKey.OP_READ);

        JsonRpc jsonrpc = new JsonRpc();
        RequestRouter router = new RequestRouter(cm, new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL));
        RequestHandler handler = new RequestHandler(router);

        this.processor = new PacketProcessor(transport, jsonrpc, handler);
        this.reader = new NonBlockingConsoleReader();
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
    }

    private void handleConsole() throws IOException {
        String input = reader.readNextLine();
        if ("exit".equalsIgnoreCase(input)) {
            stop();
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