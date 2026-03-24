package ru.spb.miwm64.moviemanager.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int port = 9999;
        int i = 0;
        ArrayList<String> tmp = new ArrayList<>(List.of(
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32003,\"message\":\"name cannot be empty\"},\"id\":1}",
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32003,\"message\":\"oscarsCount must be > 0\"},\"id\":2}",
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32001,\"message\":\"Movie not found with id 123\"},\"id\":3}",
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32602,\"message\":\"Invalid params\"},\"id\":4}",
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not found: unknownCommand\"},\"id\":5}",
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"Internal server error\"},\"id\":6}"
        ));
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP server listening on port " + port);

            byte[] buffer = new byte[1024];

            while (true) {
                // Prepare packet for incoming data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Receive data (blocking)
                socket.receive(packet);

                // Convert bytes to string
                String received = new String(packet.getData(), 0, packet.getLength());

                // Print message
                System.out.println("Received: " + received);
                String resp = tmp.get(i % 6);
                ++i;
                socket.send(new DatagramPacket(resp.getBytes(), resp.getBytes().length, packet.getAddress(), packet.getPort()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
{"jsonrpc": "2.0","result": 1,"id": 1}
 */