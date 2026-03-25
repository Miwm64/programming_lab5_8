package ru.spb.miwm64.moviemanager.server;

import ru.spb.miwm64.moviemanager.server.collectionmanager.StreamCollectionManager;
import ru.spb.miwm64.moviemanager.server.net.UDPServer;

public class Main {
    public static void main(String[] args) {
        UDPServer udpServer;
        try {
            udpServer = new UDPServer(7878, new StreamCollectionManager());
            udpServer.run();
        }
        catch (Exception e){}
    }
}