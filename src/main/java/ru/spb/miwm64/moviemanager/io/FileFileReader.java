package ru.spb.miwm64.moviemanager.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class FileFileReader implements Reader{
    FileReader fileReader;

    public FileFileReader(String fileName) throws FileNotFoundException {
        fileReader = new FileReader(new File(fileName));
    }

    @Override
    public String readNextLine() throws IOException {
        StringBuilder res = new StringBuilder();
        int i;
        while ((i = fileReader.read()) != -1) {
            res.append((char) i);
        }
        return res.toString();
    }

    @Override
    public boolean hasNextLine() {
        return true;
    }

    @Override
    public void close() throws Exception {
        fileReader.close();
    }
}

