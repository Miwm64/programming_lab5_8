package ru.spb.miwm64.moviemanager.client;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.collectionmanager.RemoteCollectionManager;
import ru.spb.miwm64.moviemanager.client.io.*;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.client.net.UDPClient;
import ru.spb.miwm64.moviemanager.client.io.ConsoleReader;
import ru.spb.miwm64.moviemanager.common.io.Reader;

import java.net.InetSocketAddress;


public class Main {
    public static void main(String[] args) {
        /*
        UDPClient cl = new UDPClient(new InetSocketAddress("localhost", 9999));
        Scanner sc = new Scanner(System.in);
        String input;
        while (true){
            input = sc.nextLine();
            if (Objects.equals(input, "/exit")){
                break;
            }
            System.out.println(cl.exchangeString(input));
        }
        */


        UDPClient udpClient = new UDPClient(new InetSocketAddress("localhost", 9999));
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