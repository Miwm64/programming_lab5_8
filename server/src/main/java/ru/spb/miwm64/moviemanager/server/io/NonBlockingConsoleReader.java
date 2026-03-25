package ru.spb.miwm64.moviemanager.server.io;

import ru.spb.miwm64.moviemanager.common.io.Reader;

import java.io.IOException;
import java.util.Scanner;

public class NonBlockingConsoleReader implements Reader {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public String readNextLine() throws IOException {
        if (System.in.available() > 0) {
            return scanner.nextLine();
        }
        return null;
    }

    @Override
    public boolean hasNextLine() {
        try {
            return System.in.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        scanner.close();
    }
}