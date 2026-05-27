package ru.spb.miwm64.moviemanager.server.collectionmanager;

import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.exceptions.InvalidValueException;
import ru.spb.miwm64.moviemanager.common.net.Batch;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;
import ru.spb.miwm64.moviemanager.server.db.SQLRepository;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

public class DbBatchCollectionManager {
    private final SQLRepository repo;

    public DbBatchCollectionManager(SQLRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    
    public VersionedObject<Movie> add(VersionedObject<Movie> vm,
                                      String userId) {
        Objects.requireNonNull(vm);
        vm.version = 1;
        if (!Objects.isNull(vm.data.getId()) && vm.data.getId() != 0) {
            try {
                if (!Objects.isNull(repo.findById(vm.data.getId()))) {
                    throw new InvalidValueException("Movie id must be unique");
                }
            }
            catch (SQLException e) {
                throw new RuntimeException("Failed to load existing movies", e);
            }
        }
        try {
            repo.insert(vm,  userId);
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to insert movie " + vm, e);
        }

        return vm;
    }

    
    public void setById(Long id, VersionedObject<Movie> vm, String userId) {
        try {
            repo.updateById(vm, vm.data.getOperator(), userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update movie", e);
        }
    }


    
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

    
    public ArrayList<VersionedObject<Movie>> getAll() {
        try {
            return new ArrayList<>(repo.findAllMovies());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch movies", e);
        }
    }

    
    public void removeById(Long id, String userId) {
        try {
            boolean removed = repo.deleteById(id, userId);
            if (!removed) {
                throw new NoSuchElementException("Movie with id " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete movie", e);
        }
    }


    
    public void removeAll() {
        try {
            repo.clearAll();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear movies", e);
        }
    }

    
    public Batch applyBatch(Batch pendingBatch, Map<Long, Integer> clientVersions,
                            String userId) {
        ArrayList<String> messages = new ArrayList<>();

        if (pendingBatch != null) {
            for (VersionedObject<Movie> vm : pendingBatch.creates) {
                try {
                    add(vm, userId);
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
                    setById(id, updated, userId);
                } catch (NoSuchElementException e) {
                    messages.add("Update failed: movie " + id + " not found");
                }
            }

            for (Long id : pendingBatch.deletes) {
                try {
                    removeById(id, userId);
                } catch (NoSuchElementException e) {
                    messages.add("Delete failed: movie " + id + " not found");
                } catch (RuntimeException e) {
                    messages.add("Failed to delete movie");
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
}