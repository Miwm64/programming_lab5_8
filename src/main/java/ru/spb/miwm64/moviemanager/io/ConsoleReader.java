package main.java.ru.spb.miwm64.moviemanager.io;

import java.util.Scanner;

public class ConsoleReader implements Reader {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public String readNextLine() {
        return scanner.nextLine();
    }

    @Override
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    public void close() {
        scanner.close();
    }
}
