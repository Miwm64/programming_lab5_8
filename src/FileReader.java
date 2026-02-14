import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileReader implements Reader{
    String filepath;
    java.io.BufferedInputStream inputStream;
    public FileReader(String filepath) throws FileNotFoundException {
        this.filepath = filepath;
        inputStream = new java.io.BufferedInputStream(new FileInputStream(this.filepath));
    }

    @Override
    public String read() throws IOException {
        StringBuilder line = new StringBuilder();
        int i;
        while((i = inputStream.read())!= -1){
            line.append((char) i);
        }
        inputStream.close();
        return line.toString();
    }

    @Override
    public String readNextLine() throws IOException {
        StringBuilder line = new StringBuilder();
        int i;
        while((i = inputStream.read())!= -1){
            if ((char) i == '\n'){
                return line.toString();
            }
            line.append((char) i);
        }
        inputStream.close();
        return line.toString();
    }

    @Override
    public boolean hasNextLine() throws IOException {
        return inputStream.available() != 0;
    }
}
