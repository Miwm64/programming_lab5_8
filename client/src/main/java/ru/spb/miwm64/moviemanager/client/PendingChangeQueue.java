package ru.spb.miwm64.moviemanager.client;

import ru.spb.miwm64.moviemanager.common.entities.Movie;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class PendingChangeQueue {
    private final ArrayList<VersionedObject<Movie>> updates = new ArrayList<>();
    private final ArrayList<VersionedObject<Movie>> creates = new ArrayList<>();
    private final ArrayList<Integer> deletes = new ArrayList<>();
    private final LinkedList<Batch> batchArray = new LinkedList<>();

    private final ReentrantLock mutex = new ReentrantLock();

    public Batch getBatch() {
        mutex.lock();
        try {
            if (!batchArray.isEmpty()) {
                return batchArray.get(0);
            }
        } finally {
            mutex.unlock();
        }

        formBatch();

        mutex.lock();
        try {
            if (!batchArray.isEmpty()) {
                return batchArray.get(0);
            }
            return null;
        } finally {
            mutex.unlock();
        }
    }

    private void formBatch() {
        mutex.lock();
        try {
            if (creates.isEmpty() && updates.isEmpty() && deletes.isEmpty()) {
                return;
            }

            Batch batch = new Batch(
                    new ArrayList<>(updates),
                    new ArrayList<>(creates),
                    new ArrayList<>(deletes)
            );
            batchArray.add(batch);

            updates.clear();
            creates.clear();
            deletes.clear();
        } finally {
            mutex.unlock();
        }
    }

    public void removeFirstBatch() {
        mutex.lock();
        try {
            if (!batchArray.isEmpty()) {
                batchArray.remove(0);
            }
        } finally {
            mutex.unlock();
        }
    }


    public void addUpdate(VersionedObject<Movie> movie) {
        mutex.lock();
        try {
            Long movieId = movie.data.getId();
            updates.removeIf(existing -> Objects.equals(existing.data.getId(), movieId));
            updates.add(movie);
        } finally {
            mutex.unlock();
        }
    }
    public void addCreate(VersionedObject<Movie> movie) {
        mutex.lock();
        try {
            creates.add(movie);
        } finally {
            mutex.unlock();
        }
    }

    public void addDelete(int movieId) {
        mutex.lock();
        try {
            deletes.add(movieId);
        } finally {
            mutex.unlock();
        }
    }
}
