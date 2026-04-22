package ru.spb.miwm64.moviemanager.common.io;

import java.io.IOException;

public interface Reader extends AutoCloseable {
    String readNextLine() throws IOException;
    boolean hasNextLine();
}
