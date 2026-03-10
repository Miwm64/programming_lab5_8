package ru.spb.miwm64.moviemanager.io;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FullStreamFileReader implements Reader {
    private final String filepath;
    private final java.io.BufferedInputStream inputStream;
    private final String fullContent;
    private boolean consumed = false;

    public FullStreamFileReader(String filepath) throws IOException {
        this.filepath = filepath;
        this.inputStream = new java.io.BufferedInputStream(new FileInputStream(this.filepath));
        this.fullContent = readFullFile();
    }

    private String readFullFile() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int i;
        while ((i = inputStream.read()) != -1) {
            buffer.write(i);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Override
    public String readNextLine() throws IOException {
        if (!consumed) {
            consumed = true;
            return fullContent.trim();
        }
        return null;
    }

    @Override
    public boolean hasNextLine() {
        return !consumed && fullContent != null && !fullContent.isEmpty();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    public String getFilepath() {
        return filepath;
    }

    public String getFullContent() {
        return fullContent;
    }
}