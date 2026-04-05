package ru.spb.miwm64.moviemanager.client;

import ru.spb.miwm64.moviemanager.common.entities.Movie;

import java.util.ArrayList;

public class Batch {
    public final ArrayList<VersionedObject<Movie>> updates;
    public final ArrayList<VersionedObject<Movie>> creates;
    public final ArrayList<Long> deletes;

    public Batch(ArrayList<VersionedObject<Movie>> updates,
                 ArrayList<VersionedObject<Movie>> creates,
                 ArrayList<Long> deletes) {
        this.updates = updates;
        this.creates = creates;
        this.deletes = deletes;
    }
}