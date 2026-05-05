package ru.spb.miwm64.moviemanager.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchStreamCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.StreamCollectionManager;
import ru.spb.miwm64.moviemanager.server.net.UDPServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        XMLParser xmlParser = new XMLParser();
        BatchCollectionManager collectionManager = new BatchStreamCollectionManager();
        UDPServer udpServer;
        String url;
        String user;
        String pass;

        url = System.getenv("DB_URL");
        user = System.getenv("DB_USER");
        pass = System.getenv("DB_PASSWORD");

        try {
            Connection con = DriverManager.getConnection(url, user, pass);
            log.info("Application started");
            udpServer = new UDPServer(7878, collectionManager, xmlParser);
            udpServer.run();
        }
        catch (IllegalStateException e) {
            System.out.println("Set XML_LOAD environment variable");
            log.error("Error: {}", e.getMessage());
        }
        catch (Exception e){
            log.error("Error: {}", e.getMessage());
        }
    }
}