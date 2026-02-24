package ru.spb.miwm64.moviemanager;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.io.BufferedFileReader;
import ru.spb.miwm64.moviemanager.io.Reader;
import ru.spb.miwm64.moviemanager.io.Writer;

import java.util.ArrayList;
import java.util.List;

public final class MainController {
    private CollectionManager collectionManager;
    private List<Reader> readers;
    private Writer writer;
    // XML parser

    public MainController(CollectionManager collectionManager, Reader defaultReader,
                          Writer defaultWriter) {
        this.collectionManager = collectionManager;
        this.readers = new ArrayList<>();
        readers.add(defaultReader);
        this.writer = defaultWriter;
    }

    public void run() {
        while (true) {
            boolean result;
            if (readers.get(0) instanceof BufferedFileReader) {
                result = fileRun();
            } else {
                result = consoleRun();
            }

            if (result) {
                break;
            }
        }
        // save xml
    }

    private boolean consoleRun() {
        return false;
    }

    private boolean fileRun() {
        return false;
    }
}
