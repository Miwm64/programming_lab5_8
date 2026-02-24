package ru.spb.miwm64.moviemanager;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.io.*;

public class Main {
    public static void main(String[] args) {
        XMLParser xmlParser = new XMLParser();
        CollectionManager collectionManager = new SortedCollectionManager();
        Reader reader = new ConsoleReader();
        Writer writer = new ConsoleWriter();
        var mainController = new MainController(collectionManager, reader, writer, xmlParser);
        mainController.run();
        return;
    }
}