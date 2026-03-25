package ru.spb.miwm64.moviemanager.server.net;

import ru.spb.miwm64.moviemanager.common.net.JsonRpcError;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacketProcessor {

    private final UDPTransport transport;
    private final JsonRpc jsonRpc;
    private final RequestHandler handler;

    public PacketProcessor(UDPTransport transport,
                           JsonRpc codec,
                           RequestHandler handler) {
        this.transport = transport;
        this.jsonRpc = codec;
        this.handler = handler;
    }

    public void process() {
        SocketAddress client = null;
        Integer id = null;

        try {
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            client = transport.receive(buffer);

            if (client == null) return;

            String json = extract(buffer);

            JsonRpcRequest request = jsonRpc.decodeRequest(json);
            id = request.id;

            Object result = handler.handle(request);

            byte[] response = jsonRpc.encodeSuccess(result, id);
            transport.send(client, response);

        } catch (Exception e) {
            if (client != null) {
                try {
                    byte[] err = jsonRpc.encodeError(
                            JsonRpcError.INTERNAL_ERROR,
                            "Internal error",
                            id
                    );
                    transport.send(client, err);
                } catch (IOException ignored) {}
            }
        }
    }

    private String extract(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}