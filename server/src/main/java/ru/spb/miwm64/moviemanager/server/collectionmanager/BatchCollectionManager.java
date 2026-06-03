package ru.spb.miwm64.moviemanager.server.collectionmanager;

import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.Batch;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

import java.util.ArrayList;
import java.util.Map;

public interface BatchCollectionManager {

    VersionedObject<Movie> add(VersionedObject<Movie> vm);

    void setById(Long id, VersionedObject<Movie> vm);

    VersionedObject<Movie> getById(Long id);

    ArrayList<VersionedObject<Movie>> getAll();

    void removeById(Long id);

    void removeAll();

    Batch applyBatch(Batch pendingBatch, Map<Long, Integer> clientVersions);

    void setCollection(ArrayList<Movie> movies);
}