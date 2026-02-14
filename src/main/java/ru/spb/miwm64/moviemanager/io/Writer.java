package ru.spb.miwm64.moviemanager.io;

import java.io.IOException;

public interface Writer extends AutoCloseable {
    void write(String line) throws IOException;
    void writeln(String line) throws IOException;
}
