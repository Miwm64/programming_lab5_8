package main.java.ru.spb.miwm64.moviemanager.io;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BufferedFileReader implements Reader {
    private final String filepath;
    private final java.io.BufferedInputStream inputStream;
    private String cachedLine;

    public BufferedFileReader(String filepath) throws IOException {
        this.filepath = filepath;
        inputStream = new java.io.BufferedInputStream(new FileInputStream(this.filepath));
        cachedLine = read();
    }


    @Override
    public String readNextLine() throws IOException {
        String current = cachedLine;
        if (cachedLine != null) {
            cachedLine = read();
        }
        return current;
    }


    public String read() throws IOException {
        int i;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while((i = inputStream.read())!= -1){
            if ((char) i == '\n'){
               break;
            }
            if ((char) i == '\r'){
                continue;
            }
            buffer.write(i);
        }

        if (buffer.size() == 0 && i == -1) {
            return null;
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Override
    public boolean hasNextLine() throws IOException {
        return cachedLine != null;
    }

    public void close() throws IOException {
        inputStream.close();
    }

    public String getFilepath() {
        return filepath;
    }
}
