package ru.spb.miwm64.moviemanager.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleFileReader implements Reader{
    private Path filepath;
    private boolean hasBeenRead = false;

    public SimpleFileReader(String filePath)  {
        this.filepath = Paths.get(filePath);
    }

    @Override
    public String readNextLine() throws IOException{
        if (!hasBeenRead) {
            String content = Files.readString(filepath);
            hasBeenRead = true;
            return content;
        }
        return null;
    }

    @Override
    public boolean hasNextLine() {
        return !hasBeenRead;
    }

    @Override
    public void close() throws IOException {
        // Nothing to close
    }
}