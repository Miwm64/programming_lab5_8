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
    private static final int MAX_PACKET_SIZE = 65536;
    private static final int DEFAULT_RESPONSE_TIMEOUT = 5000;

    private DatagramChannel datagramChannel;
    private CollectionManager collectionManager;
    private Selector selector;
    private boolean isRunning = true;
    private Reader reader;
    private RequestRouter requestRouter;

    private final static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // ← YOU HAVE THIS
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public UDPServer(int port, CollectionManager collectionManager) throws IOException {
        this.datagramChannel = DatagramChannel.open();
        this.datagramChannel.bind(new InetSocketAddress(port));
        this.datagramChannel.configureBlocking(false);

        this.selector = Selector.open();
        this.datagramChannel.register(selector, SelectionKey.OP_READ);
        this.reader = new NonBlockingConsoleReader();

        this.collectionManager = collectionManager;
        this.requestRouter = new RequestRouter(collectionManager, objectMapper);
    }

    public void run() {
        while (isRunning) {
            try {
                selector.select(100);

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isReadable()) {
                        handleSinglePacket();
                    }
                }

                String consoleInput = reader.readNextLine();
                if (consoleInput != null && consoleInput.trim().equalsIgnoreCase("exit")) {
                    stop();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public void stop() {
        isRunning = false;
        try {
            if (selector != null) selector.close();
            if (datagramChannel != null) datagramChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSinglePacket(){
        SocketAddress remoteAdd = null;
        Integer id = 0;
        try {
            // Get msg
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            remoteAdd = datagramChannel.receive(buffer);

            // Create request
            String request = extractString(buffer);
            JsonRpcRequest jsonRpcRequest = objectMapper.readValue(request, JsonRpcRequest.class);
            id = jsonRpcRequest.id;
            System.out.println("Client at #" + remoteAdd + "  sent: " + request);

            Object res = requestRouter.route(jsonRpcRequest.method, jsonRpcRequest.params);
            sendSuccessResponse(remoteAdd, res, jsonRpcRequest.id);
        }
        catch (Exception e){
            e.printStackTrace();
            if (!(remoteAdd == null)) {
                sendErrorResponse(remoteAdd, JsonRpcError.INTERNAL_ERROR, "Internal error", id);
            }
        }
    }

    private static String extractString(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String msg = new String(bytes);

        return msg;
    }
    private void sendSuccessResponse(SocketAddress client, Object result, int id) {
        try {
            JsonRpcResponse<Object> response = new JsonRpcResponse<>();
            response.id = id;
            response.result = result;
            response.error = null;

            String json = objectMapper.writeValueAsString(response);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            datagramChannel.send(buffer, client);
        } catch (IOException e) {
            System.err.println("Failed to send response: " + e.getMessage());
        }
    }

    private void sendErrorResponse(SocketAddress client, int code, String message, Integer id) {
        try {
            JsonRpcResponse<Void> response = new JsonRpcResponse<>();
            response.id = id;
            response.result = null;
            response.error = new JsonRpcError(code, message, null);

            String json = objectMapper.writeValueAsString(response);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            datagramChannel.send(buffer, client);
        } catch (IOException e) {
            System.err.println("Failed to send error: " + e.getMessage());
        }
    }
}
/*
    public void run() {}
    public void stop() {}
    private void handlePacket(){}
    class router{}
    private void sendSuccessResponse(){}
    private void sendErrorResponse(){}
 */
