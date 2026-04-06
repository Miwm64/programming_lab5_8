package ru.spb.miwm64.moviemanager.client.collectionmanager;

import ru.spb.miwm64.moviemanager.common.net.Batch;
import ru.spb.miwm64.moviemanager.client.PendingChangeQueue;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.Person;
import ru.spb.miwm64.moviemanager.common.exceptions.InvalidValueException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BatchRemoteCollectionManager implements CollectionManager {
    private final List<VersionedObject<Movie>> movies = new ArrayList<>();
    private final Map<Long, Boolean> currentIDs = new HashMap<>();
    private long lastAssignedId = 1L;

    private final PendingChangeQueue queue;

    public BatchRemoteCollectionManager(PendingChangeQueue queue) {
        this.queue = Objects.requireNonNull(queue);
    }

    public BatchRemoteCollectionManager(PendingChangeQueue queue, List<Movie> movies) {
        setCollection(new ArrayList<>(movies));
        this.queue = Objects.requireNonNull(queue);
    }

    @Override
    public int add(Movie movie) {
        Objects.requireNonNull(movie);

        if (movie.getId() != null) {
            boolean exists = movies.stream()
                    .anyMatch(vm -> Objects.equals(vm.data.getId(), movie.getId()));
            if (exists) {
                throw new InvalidValueException("Movie id must be unique");
            }
        } else {
            while (currentIDs.containsKey(lastAssignedId)) {
                lastAssignedId++;
            }
            movie.setId(-lastAssignedId);
            currentIDs.put(lastAssignedId, true);
        }
        VersionedObject<Movie> versionedMovie = new VersionedObject<>(1, movie);
        int index = Collections.binarySearch(movies, versionedMovie);
        if (index < 0) index = -index - 1;

        movies.add(index, versionedMovie);
        queue.addCreate(versionedMovie);
        return movie.getId().intValue();
    }

    @Override
    public boolean addIfMin(Movie movie) {
        return movies.stream()
                .findFirst()
                .map(min -> movie.compareTo(min.data) < 0 && add(movie) > 0)
                .orElseGet(() -> {
                    add(movie);
                    return true;
                });
    }

    public void setCollection(ArrayList<Movie> movies) {
        removeAll();
        for (Movie m : movies) {
            this.movies.add(new VersionedObject<>(1, m));
            currentIDs.put(m.getId(), true);
        }
    }

    @Override
    public void setById(Long id, Movie movie) {
        int index = findIndexById(id);
        VersionedObject<Movie> versionedMovie = new VersionedObject<>(movies.get(index).version, movie);
        movies.set(index, versionedMovie);
        queue.addUpdate(versionedMovie);
    }

    @Override
    public Movie getById(Long id) {
        return movies.stream()
                .filter(vm -> Objects.equals(vm.data.getId(), id))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Movie with id " + id + " not found"))
                .data;
    }

    @Override
    public Movie getByIndex(int index) {
        if (index < 0 || index >= movies.size()) {
            throw new InvalidValueException("Index out of bounds: " + index);
        }
        return movies.get(index).data;
    }

    @Override
    public ArrayList<Movie> getGreater(Person person) {
        Objects.requireNonNull(person);

        return movies.stream()
                .filter(vm -> vm.data.getOperator() != null)
                .filter(vm -> vm.data.getOperator().compareTo(person) > 0)
                .sorted(Comparator.comparing(vm -> vm.data.getName()))
                .map(vm -> vm.data)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Movie> getAll() {
        return movies.stream()
                .sorted(Comparator.comparing(vm -> vm.data.getName()))
                .map(vm -> vm.data)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void removeById(Long id) {
        boolean removed = movies.removeIf(vm -> Objects.equals(vm.data.getId(), id));
        if (!removed) {
            throw new NoSuchElementException("Movie with id " + id + " not found");
        }
        currentIDs.remove(id);
        queue.addDelete(id);
    }

    @Override
    public void removeByIndex(int index) {
        if (index < 0 || index >= movies.size()) {
            throw new InvalidValueException("Index is out of bounds: " + index);
        }
        Long id = movies.get(index).data.getId();
        movies.remove(index);
        currentIDs.remove(id);
        queue.addDelete(id);
    }

    @Override
    public void removeGreater(Movie movie) {
        Objects.requireNonNull(movie);
        List<Long> toRemove = movies.stream()
                .filter(vm -> vm.data.compareTo(movie) > 0)
                .map(vm -> vm.data.getId())
                .collect(Collectors.toList());
        toRemove.forEach(this::removeById);
    }

    @Override
    public void removeAll() {
        List<Long> ids = movies.stream()
                .map(vm -> vm.data.getId())
                .collect(Collectors.toList());
        ids.forEach(this::removeById);
        currentIDs.clear();
        lastAssignedId = 1L;
    }

    @Override
    public void clear() {
        removeAll();
    }

    @Override
    public long countByGoldenPalmCount(long count) {
        return movies.stream()
                .filter(vm -> vm.data.getGoldenPalmCount() == count)
                .count();
    }

    @Override
    public ArrayList<Movie> filterGreaterThanOperatorCommand(Person p) {
        return movies.stream()
                .filter(vm -> vm.data.getOperator() != null)
                .filter(vm -> vm.data.getOperator().compareTo(p) > 0)
                .sorted(Comparator.comparing(vm -> vm.data.getName()))
                .map(vm -> vm.data)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Movie> printFieldAscendingGoldenPalmCountCommand() {
        return movies.stream()
                .sorted(Comparator.comparingLong(vm -> vm.data.getGoldenPalmCount()))
                .map(vm -> vm.data)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private int findIndexById(Long id) {
        return IntStream.range(0, movies.size())
                .filter(i -> Objects.equals(movies.get(i).data.getId(), id))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Movie with id " + id + " not found"));
    }

    public void applyRemoteBatch(Batch batch) {
        if (batch == null) return;

        for (Long id : batch.deletes) {
            try {
                removeById(id);
            } catch (Exception ignored){}
        }

        for (VersionedObject<Movie> vm : batch.updates) {
            try {
                updateFromServer(vm);
            } catch (Exception ignored){}
        }

        for (VersionedObject<Movie> vm : batch.creates) {
            try {
                addFromServer(vm);
            } catch (Exception ignored){}

        }

        movies.removeIf(vm -> vm.data.getId() < 0);
        currentIDs.keySet().removeIf(id -> id < 0);
    }

    private void updateFromServer(VersionedObject<Movie> serverMovie) {
        int index = findIndexById(serverMovie.data.getId());
        movies.set(index, serverMovie);
    }

    private void addFromServer(VersionedObject<Movie> serverMovie) {
        int index = Collections.binarySearch(movies, serverMovie);
        if (index < 0) index = -index - 1;
        movies.add(index, serverMovie);
        currentIDs.put(serverMovie.data.getId(), true);
    }

    public Map<Long, Integer> getVersionMap() {
        Map<Long, Integer> map = new HashMap<>();
        for (VersionedObject<Movie> vm : movies) {
            map.put(vm.data.getId(), vm.version);
        }
        return map;
    }
}