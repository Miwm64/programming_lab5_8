package ru.spb.miwm64.moviemanager.client.io;

import ru.spb.miwm64.moviemanager.common.io.Reader;

import java.util.Scanner;

public class ConsoleReader implements Reader {
    private Scanner scanner = new Scanner(System.in);

    @Override
    public String readNextLine() {
        String text = "";
        try {
            text = scanner.nextLine();
        }
        catch (Exception e) {
            scanner = new Scanner(System.in);
        }
        return text;
    }

    @Override
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    public void close() {
        scanner.close();
    }
}
