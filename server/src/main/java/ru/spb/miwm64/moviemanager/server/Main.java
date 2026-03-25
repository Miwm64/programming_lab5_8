package ru.spb.miwm64.moviemanager.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        UDPServer udpServer;
        try {
            udpServer = new UDPServer(7878, new SortedCollectionManager());
            udpServer.run();
        }
        catch (Exception e){}
    }
}