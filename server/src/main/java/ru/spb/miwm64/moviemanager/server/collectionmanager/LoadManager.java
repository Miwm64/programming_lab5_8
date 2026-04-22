package ru.spb.miwm64.moviemanager.server.collectionmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public class LoadManager {
    private static final String ENV_VARIABLE = "XML_LOAD";

    private final BatchStreamCollectionManager collectionManager;
    private final XMLParser xmlParser;
    private Logger log = LoggerFactory.getLogger(LoadManager.class);

    public LoadManager(BatchStreamCollectionManager collectionManager, XMLParser xmlParser) {
        this.collectionManager = Objects.requireNonNull(collectionManager);
        this.xmlParser = Objects.requireNonNull(xmlParser);
    }

    public void loadCollection() {
        try {
            String xml = readFile();
            collectionManager.setCollection(xmlParser.parseFromXMLCollection(xml));
            System.out.println("Loaded collection successfully");
        }
        catch (Exception e){
            log.error("Couldn't load collection: " + e.getMessage());
        }
    }

    private String readFile() throws IOException {
        if (System.getenv(ENV_VARIABLE) != null && !Files.exists(Path.of(System.getenv(ENV_VARIABLE)))) {
            Files.createFile(Path.of(System.getenv(ENV_VARIABLE)));
        }
        try {
            return Files.readString(Path.of(getPath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read XML file", e);
        }
    }

    public void saveCollection() {
        ArrayList<VersionedObject<Movie>> versionedMovies = collectionManager.getAll();
        ArrayList<Movie> movies = new ArrayList<>();
        for (var vm : versionedMovies){
            movies.add(vm.data);
        }
        String xml = xmlParser.parseCollectionIntoXML(movies);
        writeFile(xml);
        System.out.println("Saved collection successfully");
    }

    private void writeFile(String content) {
        Path path = Path.of(getPath());
        Path temp = Path.of(path + ".tmp");

        try {
            Files.writeString(temp, content); // overwrite temp
            Files.move(temp, path,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write XML file", e);
        }
    }

    private String getPath() {
        String path = System.getenv(ENV_VARIABLE);
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Environment variable " + ENV_VARIABLE + " is not set");
        }
        return path;
    }
}