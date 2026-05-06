package ru.spb.miwm64.moviemanager.server.collectionmanager;

import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.exceptions.InvalidValueException;
import ru.spb.miwm64.moviemanager.common.net.Batch;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;
import ru.spb.miwm64.moviemanager.server.db.SQLRepository;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

public class DbBatchCollectionManager implements BatchCollectionManager {
    private final SQLRepository repo;
    private final Map<Long, Boolean> currentIDs = new HashMap<>();
    private long lastAssignedId = 1L;

    public DbBatchCollectionManager(SQLRepository repo) {
        this.repo = Objects.requireNonNull(repo);
        try {
            for (VersionedObject<Movie> vm : repo.findAllMovies()) {
                Long id = vm.data.getId();
                currentIDs.put(id, true);
                if (id >= lastAssignedId) {
                    lastAssignedId = id + 1;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load existing movies", e);
        }
    }

    @Override
    public VersionedObject<Movie> add(VersionedObject<Movie> vm) {
        Objects.requireNonNull(vm);

        if (vm.data.getId() != null && vm.data.getId() > 0) {
            if (currentIDs.containsKey(vm.data.getId())) {
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

        // Insert into DB
        try {
            repo.insert(vm);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add movie", e);
        }

        return vm;
    }

    @Override
    public void setById(Long id, VersionedObject<Movie> vm) {
        try {
            repo.updateById(vm, vm.data.getOperator());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update movie", e);
        }
    }

    private int findIndexById(Long id) {
        List<VersionedObject<Movie>> all = getAll();
        return IntStream.range(0, all.size())
                .filter(i -> Objects.equals(all.get(i).data.getId(), id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Movie with id " + id + " not found"));
    }

    @Override
    public VersionedObject<Movie> getById(Long id) {
        try {
            VersionedObject<Movie> result = repo.findById(id);
            if (result == null) {
                throw new NoSuchElementException("Movie with id " + id + " not found");
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch movie", e);
        }
    }

    @Override
    public VersionedObject<Movie> getByIndex(int index) {
        List<VersionedObject<Movie>> all = getAll();
        if (index < 0 || index >= all.size()) {
            throw new InvalidValueException("Index out of bounds: " + index);
        }
        return all.get(index);
    }

    @Override
    public ArrayList<VersionedObject<Movie>> getAll() {
        try {
            return new ArrayList<>(repo.findAllMovies());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch movies", e);
        }
    }

    @Override
    public void removeById(Long id) {
        try {
            boolean removed = repo.deleteById(id);
            if (!removed) {
                throw new NoSuchElementException("Movie with id " + id + " not found");
            }
            currentIDs.remove(id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete movie", e);
        }
    }

    @Override
    public void removeByIndex(int index) {
        List<VersionedObject<Movie>> all = getAll();
        if (index < 0 || index >= all.size()) {
            throw new InvalidValueException("Index out of bounds: " + index);
        }
        Long id = all.get(index).data.getId();
        removeById(id);
    }

    @Override
    public void removeGreater(VersionedObject<Movie> threshold) {
        Objects.requireNonNull(threshold);
        List<VersionedObject<Movie>> all = getAll();
        all.stream()
                .filter(m -> m.compareTo(threshold) > 0)
                .forEach(m -> removeById(m.data.getId()));
    }

    @Override
    public void removeAll() {
        try {
            repo.clearAll();
            currentIDs.clear();
            lastAssignedId = 1L;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear movies", e);
        }
    }

    @Override
    public Batch applyBatch(Batch pendingBatch, Map<Long, Integer> clientVersions) {
        ArrayList<String> messages = new ArrayList<>();

        if (pendingBatch != null) {
            for (VersionedObject<Movie> vm : pendingBatch.creates) {
                try {
                    add(vm);
                } catch (Exception e) {
                    messages.add("Failed to create movie: " + e.getMessage());
                }
            }

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

            for (Long id : pendingBatch.deletes) {
                try {
                    removeById(id);
                } catch (NoSuchElementException e) {
                    messages.add("Delete failed: movie " + id + " not found");
                }
            }
        }


        ArrayList<VersionedObject<Movie>> deltaCreates = new ArrayList<>();
        ArrayList<VersionedObject<Movie>> deltaUpdates = new ArrayList<>();
        ArrayList<Long> deltaDeletes = new ArrayList<>();

        if (clientVersions == null || clientVersions.isEmpty()) {
            deltaCreates = getAll();
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

    @Override
    public void setCollection(ArrayList<Movie> movies) {
        removeAll();

        for (Movie m : movies) {
            if (m.getId() == null || m.getId() <= 0) {
                throw new InvalidValueException("Movie must have a positive ID for server collection");
            }
            VersionedObject<Movie> vm = new VersionedObject<>(1, m);
            add(vm);
        }
    }
}