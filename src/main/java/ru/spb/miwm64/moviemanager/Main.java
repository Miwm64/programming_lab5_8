package ru.spb.miwm64.moviemanager;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.io.*;

import java.sql.Array;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
//        MyComparator cmp = new MyComparator();
//        System.out.println(cmp.compare(1.0, 1.0));
//        System.out.println(cmp.compare(2.0, 1.0));
//        System.out.println(cmp.compare(-1.0, 1.0));
//        cmp.setValue1(1.0);
//        System.out.println(cmp.compare(1.0));
//        System.out.println(cmp.compare(2.0));
//        System.out.println(cmp.compare(3.0));
//        System.out.println(cmp.getMyClass());
//        System.out.println(cmp.getValClass(new ArrayList<String>(
//                Arrays.asList(
//                        "123","213"
//                )
//        )));

        // Buffered
        LocalDateTime start = LocalDateTime.now();
        for (var i = 0; i < 1; ++i) {
            try {
                Reader reader = new BufferedFileReader("input3.txt");
                reader.readNextLine();
            }
            catch (Exception e) {

            }
        }
        LocalDateTime end = LocalDateTime.now();
        System.out.println("Buffered: " + Duration.between(start, end).toNanos());

//        // Scanner
//        start = LocalDateTime.now();
//        for (var i = 0; i < 1; ++i) {
//            try {
//                Reader reader = new SimpleFileReader("input3.txt");
//                reader.readNextLine();
//            }
//            catch (Exception e) {
//
//            }
//        }
//        end = LocalDateTime.now();
//        System.out.println("Simple: " + Duration.between(start, end).toNanos());

        // File
        start = LocalDateTime.now();
        for (var i = 0; i < 1; ++i) {
            try {
                Reader reader = new FileFileReader("input3.txt");
                reader.readNextLine();
            }
            catch (Exception e) {

            }
        }
        end = LocalDateTime.now();
        System.out.println("File: " + Duration.between(start, end).toNanos());



        XMLParser xmlParser = new XMLParser();
        CollectionManager collectionManager = new SortedCollectionManager();
        Reader reader = new ConsoleReader();
        Writer writer = new ConsoleWriter();
        var mainController = new MainController(collectionManager, reader, writer, xmlParser);
        mainController.run();

        return;

    }
}