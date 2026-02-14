package ru.spb.miwm64.moviemanager.io;

import java.io.IOException;

public class FileWriter implements Writer {
    private final String filepath;
    private final java.io.FileWriter  fileWriter;
    private final boolean append;

    public FileWriter(String filepath, boolean append) throws IOException {
        this.filepath = filepath;
        this.append = append;
        fileWriter = new java.io.FileWriter(filepath, this.append);
    }

    public FileWriter(String filepath) throws IOException {
        this.filepath = filepath;
        append = false;
        fileWriter = new java.io.FileWriter(filepath, this.append);
    }

    @Override
    public void write(String line) throws IOException {
        fileWriter.write(line);
    }

    @Override
    public void writeln(String line) throws IOException {
        fileWriter.write(line+"\r\n");
    }

    @Override
    public void close() throws Exception {
        fileWriter.close();
    }

    public String getFilepath() {
        return filepath;
    }

    public boolean isAppend() {
        return append;
    }
}
