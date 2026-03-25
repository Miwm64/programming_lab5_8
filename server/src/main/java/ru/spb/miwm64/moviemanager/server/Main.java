package ru.spb.miwm64.moviemanager.server;

import ru.spb.miwm64.moviemanager.server.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.server.net.UDPServer;

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