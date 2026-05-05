package ru.spb.miwm64.moviemanager.server.db;

import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.Batch;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchCollectionManager;

import java.util.ArrayList;
import java.util.Map;

public class DbBatchCollectionManager implements BatchCollectionManager {

    @Override
    public VersionedObject<Movie> add(VersionedObject<Movie> vm) {
        return null;
    }

    @Override
    public void setById(Long id, VersionedObject<Movie> vm) {

    }

    @Override
    public VersionedObject<Movie> getById(Long id) {
        return null;
    }

    @Override
    public VersionedObject<Movie> getByIndex(int index) {
        return null;
    }

    @Override
    public ArrayList<VersionedObject<Movie>> getAll() {
        return null;
    }

    @Override
    public void removeById(Long id) {

    }

    @Override
    public void removeByIndex(int index) {

    }

    @Override
    public void removeGreater(VersionedObject<Movie> vm) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public Batch applyBatch(Batch pendingBatch, Map<Long, Integer> clientVersions) {
        return null;
    }

    @Override
    public void setCollection(ArrayList<Movie> movies) {

    }
}
