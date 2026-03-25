package ru.spb.miwm64.moviemanager.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.collectionmanager.RemoteCollectionManager;
import ru.spb.miwm64.moviemanager.client.io.*;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.client.net.UDPClient;
import ru.spb.miwm64.moviemanager.client.io.ConsoleReader;
import ru.spb.miwm64.moviemanager.common.io.Reader;
import ru.spb.miwm64.moviemanager.common.io.Writer;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;

import java.net.InetSocketAddress;


public class Main {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("Application started");
        UDPClient udpClient = new UDPClient(new InetSocketAddress("localhost", 7878));
        JsonRpcClient jsonRpcClient = new JsonRpcClient(udpClient);
        CollectionManager collectionManager = new RemoteCollectionManager(jsonRpcClient);

        XMLParser xmlParser = new XMLParser();
        Reader reader = new ConsoleReader();
        Writer writer = new ConsoleWriter();
        var mainController = new MainController(collectionManager, reader, writer, xmlParser);
        mainController.run();

        return;
    }
}