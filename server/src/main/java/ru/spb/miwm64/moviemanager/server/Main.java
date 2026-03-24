package ru.spb.miwm64.moviemanager.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {
    public static void main(String[] args) {
        int port = 9999;

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
                String resp = "{\"jsonrpc\": \"2.0\",\"result\": 32,\"id\": 1}";
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