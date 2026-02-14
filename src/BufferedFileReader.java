import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BufferedFileReader implements Reader {
    private String filepath;
    private java.io.BufferedInputStream inputStream;

    public BufferedFileReader(String filepath) throws FileNotFoundException {
        this.filepath = filepath;
        inputStream = new java.io.BufferedInputStream(new FileInputStream(this.filepath));
    }


    @Override
    public String readNextLine() throws IOException {
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
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Override
    public boolean hasNextLine() throws IOException {
        return inputStream.available() != 0;
    }

    public void close() throws IOException {
        inputStream.close();
    }

    public String getFilepath() {
        return filepath;
    }
}
