package ru.spb.miwm64.moviemanager.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ScannerFileReader implements Reader{
    Scanner scanner;

    public ScannerFileReader(String fileName) throws FileNotFoundException {
        scanner = new Scanner(new File(fileName));
    }

    @Override
    public String readNextLine() {
        StringBuilder res = new StringBuilder();
        while (scanner.hasNextLine()) {
            res.append(scanner.nextLine());
        }
         return res.toString();
    }

    @Override
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    @Override
    public void close() throws Exception {
        scanner.close();
    }
}
