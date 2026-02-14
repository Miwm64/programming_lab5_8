package main.java.ru.spb.miwm64.moviemanager.io;

import java.io.IOException;

public class FileWriter implements Writer {
    private final String filepath;
    private final java.io.FileWriter  fileWriter;

    public FileWriter(String filepath) throws IOException {
        this.filepath = filepath;
        fileWriter = new java.io.FileWriter(filepath);
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
    public void append(String line) throws IOException {
        fileWriter.append(line);
    }

    @Override
    public void appendln(String line) throws IOException {
        fileWriter.append(line).append("\r\n");
    }

    @Override
    public void close() throws Exception {
        fileWriter.close();
    }

    public String getFilepath() {
        return filepath;
    }
}
