import java.io.IOException;

public interface Reader extends AutoCloseable {
    String readNextLine() throws IOException;
    boolean hasNextLine() throws IOException;
}
