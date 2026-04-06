package ru.spb.miwm64.moviemanager.server.collectionmanager;

import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.exceptions.InvalidValueException;
import ru.spb.miwm64.moviemanager.common.net.Batch;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

import java.util.*;
import java.util.stream.IntStream;

public class BatchStreamCollectionManager {
    private final List<VersionedObject<Movie>> versionedMovies = new ArrayList<>();
    private final Map<Long, Boolean> currentIDs = new HashMap<>();
    private long lastAssignedId = 1L;

    public VersionedObject<Movie> add(VersionedObject<Movie> vm) {
        Objects.requireNonNull(vm);

        if (vm.data.getId() != null && vm.data.getId() > 0) {
            boolean exists = versionedMovies.stream()
                    .anyMatch(m -> Objects.equals(m.data.getId(), vm.data.getId()));
            if (exists) {
                throw new InvalidValueException("Movie id must be unique");
            }
        } else {
            while (currentIDs.containsKey(lastAssignedId)) {
                lastAssignedId++;
            }
            vm.data.setId(lastAssignedId);
            currentIDs.put(lastAssignedId, true);
            vm.version = 1;
        }

        int index = Collections.binarySearch(versionedMovies, vm);
        if (index < 0) index = -index - 1;

        versionedMovies.add(index, vm);
        return vm;
    }

    public void setById(Long id, VersionedObject<Movie> vm) {
        int index = findIndexById(id);
        versionedMovies.set(index, vm);
    }

    private int findIndexById(Long id) {
        return IntStream.range(0, versionedMovies.size())
                .filter(i -> Objects.equals(versionedMovies.get(i).data.getId(), id))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Movie with id " + id + " not found"));
    }

    public VersionedObject<Movie> getById(Long id) {
        return versionedMovies.stream()
                .filter(m -> Objects.equals(m.data.getId(), id))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Movie with id " + id + " not found"));
    }

    public VersionedObject<Movie> getByIndex(int index) {
        if (index < 0 || index >= versionedMovies.size()) {
            throw new InvalidValueException("Index out of bounds: " + index);
        }
        return versionedMovies.get(index);
    }

    public ArrayList<VersionedObject<Movie>> getAll() {
        return new ArrayList<>(versionedMovies);
    }

    public void removeById(Long id) {
        boolean removed = versionedMovies.removeIf(m -> Objects.equals(m.data.getId(), id));
        if (!removed) {
            throw new NoSuchElementException("Movie with id " + id + " not found");
        }
        currentIDs.remove(id);
    }

    public void removeByIndex(int index) {
        if (index < 0 || index >= versionedMovies.size()) {
            throw new InvalidValueException("Index out of bounds: " + index);
        }
        Long id = versionedMovies.get(index).data.getId();
        versionedMovies.remove(index);
        currentIDs.remove(id);
    }

    public void removeGreater(VersionedObject<Movie> vm) {
        Objects.requireNonNull(vm);
        versionedMovies.removeIf(m -> m.compareTo(vm) > 0);
    }

    public void removeAll() {
        versionedMovies.clear();
        currentIDs.clear();
        lastAssignedId = 1L;
    }


    public Batch applyBatch(Batch pendingBatch, Map<Long, Integer> clientVersions) {
        ArrayList<String> messages = new ArrayList<>();

        if (pendingBatch != null) {
            // Creates
            for (VersionedObject<Movie> vm : pendingBatch.creates) {
                try {
                    add(vm);
                } catch (Exception e) {
                    messages.add("Failed to create movie: " + e.getMessage());
                }
            }
            // Updates
            for (VersionedObject<Movie> vm : pendingBatch.updates) {
                Long id = vm.data.getId();
                try {
                    VersionedObject<Movie> current = getById(id);
                    if (vm.version != current.version) {
                        messages.add("Update rejected for movie " + id +
                                ": client version " + vm.version +
                                " vs server " + current.version);
                        continue;
                    }
                    int newVersion = current.version + 1;
                    VersionedObject<Movie> updated = new VersionedObject<>(newVersion, vm.data);
                    setById(id, updated);
                } catch (NoSuchElementException e) {
                    messages.add("Update failed: movie " + id + " not found");
                }
            }
            // Deletes
            for (Long id : pendingBatch.deletes) {
                try {
                    removeById(id);
                } catch (NoSuchElementException e) {
                    messages.add("Delete failed: movie " + id + " not found");
                }
            }
        }

        // delta
        ArrayList<VersionedObject<Movie>> deltaCreates = new ArrayList<>();
        ArrayList<VersionedObject<Movie>> deltaUpdates = new ArrayList<>();
        ArrayList<Long> deltaDeletes = new ArrayList<>();

        if (clientVersions == null || clientVersions.isEmpty()) {
            deltaCreates = getAll();
            messages.add("Client had no version map – full sync");
        } else {
            for (VersionedObject<Movie> serverMovie : getAll()) {
                Long id = serverMovie.data.getId();
                int serverVersion = serverMovie.version;
                Integer clientVersion = clientVersions.get(id);
                if (clientVersion == null || clientVersion < 0) {
                    deltaCreates.add(serverMovie);
                } else if (serverVersion > clientVersion) {
                    deltaUpdates.add(serverMovie);
                }
            }
            for (Long id : clientVersions.keySet()) {
                try {
                    getById(id);
                } catch (NoSuchElementException e) {
                    deltaDeletes.add(id);
                }
            }
        }

        return new Batch(deltaCreates, deltaUpdates, deltaDeletes, messages);
    }

    public void setCollection(ArrayList<Movie> movies) {
        // Clear existing data
        versionedMovies.clear();
        currentIDs.clear();
        lastAssignedId = 1L;

        // Add each movie as VersionedObject with version 1
        for (Movie m : movies) {
            if (m.getId() == null || m.getId() <= 0) {
                throw new InvalidValueException("Movie must have a positive ID for server collection");
            }
            VersionedObject<Movie> vm = new VersionedObject<>(1, m);
            int index = Collections.binarySearch(versionedMovies, vm);
            if (index < 0) index = -index - 1;
            versionedMovies.add(index, vm);
            currentIDs.put(m.getId(), true);
            if (m.getId() >= lastAssignedId) {
                lastAssignedId = m.getId() + 1;
            }
        }
    }
}