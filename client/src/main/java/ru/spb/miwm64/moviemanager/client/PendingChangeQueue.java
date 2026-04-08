package ru.spb.miwm64.moviemanager.client;

import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.Batch;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class PendingChangeQueue {
    private final ArrayList<VersionedObject<Movie>> updates = new ArrayList<>();
    private final ArrayList<VersionedObject<Movie>> creates = new ArrayList<>();
    private final ArrayList<Long> deletes = new ArrayList<>();
    private final LinkedList<Batch> batchArray = new LinkedList<>();

    private final static ReentrantLock mutex = new ReentrantLock();

    public Batch getBatch() {
        mutex.lock();
        System.out.println("getBatch");
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
                    new ArrayList<>(creates),
                    new ArrayList<>(updates),
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
            if (creates.removeIf(existing -> Objects.equals(existing.data.getId(), movieId))){
                creates.add(movie);
                return;
            }
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

    public void addDelete(Long movieId) {
        mutex.lock();
        try {
            if (creates.removeIf(vm -> Objects.equals(vm.data.getId(), movieId))){
                return;
            }
            if (updates.removeIf(vm -> Objects.equals(vm.data.getId(), movieId))){
                return;
            }
            if (movieId > 0) {
                deletes.add(movieId);
            }
        } finally {
            mutex.unlock();
        }
    }
}
