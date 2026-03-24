package ru.spb.miwm64.moviemanager.client;

import ru.spb.miwm64.moviemanager.client.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.client.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.client.io.*;

import java.net.InetSocketAddress;


public class Main {
    public static void main(String[] args) {
        UDPClient cl = new UDPClient(new InetSocketAddress("localhost", 9999));
        cl.sendPacket("Hello, World!");
        /*
        XMLParser xmlParser = new XMLParser();
        CollectionManager collectionManager = new SortedCollectionManager();
        Reader reader = new ConsoleReader();
        Writer writer = new ConsoleWriter();
        var mainController = new MainController(collectionManager, reader, writer, xmlParser);
        mainController.run();
         */
        return;
    }
}