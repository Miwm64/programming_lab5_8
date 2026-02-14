import java.io.IOException;

public interface Reader {
    String read() throws IOException;
    String readNextLine() throws IOException;
    boolean hasNextLine() throws IOException;
}
