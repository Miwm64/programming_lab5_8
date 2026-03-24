package ru.spb.miwm64.moviemanager.client;

import ru.spb.miwm64.moviemanager.client.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.client.collectionmanager.RemoteCollectionManager;
import ru.spb.miwm64.moviemanager.client.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.client.io.*;
import ru.spb.miwm64.moviemanager.client.net.UDPClient;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Scanner;


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


        XMLParser xmlParser = new XMLParser();
        UDPClient udpClient = new UDPClient(new InetSocketAddress("localhost", 9999));
        CollectionManager collectionManager = new RemoteCollectionManager(udpClient);
        Reader reader = new ConsoleReader();
        Writer writer = new ConsoleWriter();
        var mainController = new MainController(collectionManager, reader, writer, xmlParser);
        mainController.run();

        return;
    }
}