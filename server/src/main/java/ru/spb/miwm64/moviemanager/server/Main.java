package ru.spb.miwm64.moviemanager.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.server.collectionmanager.StreamCollectionManager;
import ru.spb.miwm64.moviemanager.server.net.UDPServer;

public class Main {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        XMLParser xmlParser = new XMLParser();
        CollectionManager collectionManager = new StreamCollectionManager();
        UDPServer udpServer;
        try {
            log.info("Application started");
            udpServer = new UDPServer(7878, collectionManager, xmlParser);
            udpServer.run();
        }
        catch (Exception e){
            log.error("Error: {}", e.getMessage());
        }
    }
}