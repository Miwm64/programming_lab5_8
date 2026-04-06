package ru.spb.miwm64.moviemanager.common.net;

import ru.spb.miwm64.moviemanager.common.entities.Movie;

import java.util.ArrayList;

public class Batch {
    public ArrayList<VersionedObject<Movie>> updates;
    public ArrayList<VersionedObject<Movie>> creates;
    public ArrayList<Long> deletes;
    public ArrayList<String> messages; // optional

    public Batch(){};

    public Batch(ArrayList<VersionedObject<Movie>> creates,
                 ArrayList<VersionedObject<Movie>> updates,
                 ArrayList<Long> deletes) {
        this.updates = updates;
        this.creates = creates;
        this.deletes = deletes;
    }

    public Batch(ArrayList<VersionedObject<Movie>> creates,
                 ArrayList<VersionedObject<Movie>> updates,
                 ArrayList<Long> deletes,
                 ArrayList<String> messages) {
        this.updates = updates;
        this.creates = creates;
        this.deletes = deletes;
        this.messages = messages;
    }
}