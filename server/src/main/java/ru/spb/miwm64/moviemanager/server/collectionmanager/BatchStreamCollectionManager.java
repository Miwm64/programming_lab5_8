package ru.spb.miwm64.moviemanager.server.collectionmanager;

import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.Person;
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


    public Batch applyBatch(Batch clientBatch) {
        ArrayList<String> messages = new ArrayList<>();
        ArrayList<VersionedObject<Movie>> resultCreates = new ArrayList<>();
        ArrayList<VersionedObject<Movie>> resultUpdates = new ArrayList<>();
        ArrayList<Long> resultDeletes = new ArrayList<>();

        if (clientBatch == null) {
            return new Batch(resultCreates, resultUpdates, resultDeletes, messages);
        }

        for (VersionedObject<Movie> vm : clientBatch.creates) {
            try {
                VersionedObject<Movie> created = add(vm);
                resultCreates.add(created);
            } catch (Exception e) {
                messages.add("Failed to create movie: " + e.getMessage());
            }
        }
        for (VersionedObject<Movie> vm : clientBatch.updates) {
            Long id = vm.data.getId();
            try {
                VersionedObject<Movie> current = getById(id);

                // Check version
                if (vm.version != current.version) {
                    messages.add("Update rejected for movie " + id +
                            ": client version " + vm.version +
                            " does not match server version " + current.version);
                    continue;
                }
                int newVersion = current.version + 1;
                VersionedObject<Movie> updated = new VersionedObject<>(newVersion, vm.data);
                setById(id, updated);
                resultUpdates.add(updated);
            } catch (NoSuchElementException e) {
                messages.add("Update failed: movie " + id + " not found");
            }
        }

        for (Long id : clientBatch.deletes) {
            try {
                removeById(id);
                resultDeletes.add(id);
                messages.add("Deleted movie " + id);
            } catch (NoSuchElementException e) {
                messages.add("Delete failed: movie " + id + " not found");
            }
        }

        return new Batch(resultCreates, resultUpdates, resultDeletes, messages);
    }
}