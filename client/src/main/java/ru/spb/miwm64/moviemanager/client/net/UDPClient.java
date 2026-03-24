package ru.spb.miwm64.moviemanager.client.io;
import java.net.*;


public class UDPClient {
    SocketAddress socketAddress;
    DatagramSocket socket;

    public UDPClient(SocketAddress socketAddress) {
        try {
            this.socketAddress = socketAddress;
            socket = new DatagramSocket();
        }
        catch (SocketException e) {
            System.err.println("Socket exception: " + e.getMessage());
        }
    }

    public String sendPacket(String json) {
        try {
            byte[] buffer = json.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, socketAddress);
            socket.send(packet);

            System.out.println("Sent: " + json);

            byte[] responseBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(
                    responseBuffer,
                    responseBuffer.length
            );
            socket.receive(responsePacket);

            String response = new String(
                    responsePacket.getData(),
                    0,
                    responsePacket.getLength()
            );

            System.out.println("Received: " + response);
        } catch (Exception e) {
            System.err.println("Send packet exception: " + e.getMessage());
        }
        return json;
    }
}
