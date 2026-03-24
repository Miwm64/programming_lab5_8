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
                socket.send(new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort()));
                System.out.println("Send: " + received);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}