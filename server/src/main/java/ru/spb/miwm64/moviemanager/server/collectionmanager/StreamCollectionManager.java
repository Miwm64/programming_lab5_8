package ru.spb.miwm64.moviemanager.server.collectionmanager;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.Person;
import ru.spb.miwm64.moviemanager.common.exceptions.InvalidValueException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamCollectionManager implements CollectionManager {
    private final List<Movie> movies = new ArrayList<>();
    private final Map<Long, Boolean> currentIDs = new HashMap<>();
    private long lastAssignedId = 1L;

    public StreamCollectionManager() {}
    public StreamCollectionManager(List<Movie> movies) {
        setCollection(new ArrayList<>(movies));
    }

    @Override
    public int add(Movie movie) {
        Objects.requireNonNull(movie);

        if (movie.getId() != null) {
            boolean exists = movies.stream()
                    .anyMatch(m -> Objects.equals(m.getId(), movie.getId()));
            if (exists) {
                throw new InvalidValueException("Movie id must be unique");
            }
        } else {
            while (currentIDs.containsKey(lastAssignedId)) {
                lastAssignedId++;
            }
            movie.setId(lastAssignedId);
            currentIDs.put(lastAssignedId, true);
        }

        int index = Collections.binarySearch(movies, movie);
        if (index < 0) index = -index - 1;

        movies.add(index, movie);
        return movie.getId().intValue();
    }

    @Override
    public boolean addIfMin(Movie movie) {
        return movies.stream()
                .findFirst()
                .map(min -> movie.compareTo(min) < 0 && add(movie) > 0)
                .orElseGet(() -> {
                    add(movie);
                    return true;
                });
    }

    public void setCollection(ArrayList<Movie> movies) {
        removeAll();
        this.movies.addAll(movies);
        movies.forEach(m -> currentIDs.put(m.getId(), true));
    }

    @Override
    public void setById(Long id, Movie movie) {
        int index = findIndexById(id);
        movies.set(index, movie);
    }

    @Override
    public Movie getById(Long id) {
        return movies.stream()
                .filter(m -> Objects.equals(m.getId(), id))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Movie with id " + id + " not found"));
    }

    @Override
    public Movie getByIndex(int index) {
        if (index < 0 || index >= movies.size()) {
            throw new InvalidValueException("Index out of bounds: " + index);
        }
        return movies.get(index);
    }

    @Override
    public ArrayList<Movie> getGreater(Person person) {
        Objects.requireNonNull(person);

        return movies.stream()
                .filter(m -> m.getOperator() != null)
                .filter(m -> m.getOperator().compareTo(person) > 0)
                .sorted(Comparator.comparing(Movie::getName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Movie> getAll() {
        return movies.stream()
                .sorted(Comparator.comparing(Movie::getName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void removeById(Long id) {
        boolean removed = movies.removeIf(m -> Objects.equals(m.getId(), id));
        if (!removed) {
            throw new NoSuchElementException("Movie with id " + id + " not found");
        }
        currentIDs.remove(id);
    }

    @Override
    public void removeByIndex(int index) {
        if (index < 0 || index >= movies.size()) {
            throw new InvalidValueException("Index is out of bounds: " + index);
        }
        Long id = movies.get(index).getId();
        movies.remove(index);
        currentIDs.remove(id);
    }

    @Override
    public void removeGreater(Movie movie) {
        Objects.requireNonNull(movie);

        movies.removeIf(m -> m.compareTo(movie) > 0);
    }

    @Override
    public void removeAll() {
        movies.clear();
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
                .filter(m -> m.getGoldenPalmCount() == count)
                .count();
    }

    @Override
    public ArrayList<Movie> filterGreaterThanOperatorCommand(Person p) {
        return movies.stream()
                .filter(m -> m.getOperator() != null)
                .filter(m -> m.getOperator().compareTo(p) > 0)
                .sorted(Comparator.comparing(Movie::getName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Movie> printFieldAscendingGoldenPalmCountCommand() {
        return movies.stream()
                .sorted(Comparator.comparingLong(Movie::getGoldenPalmCount))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private int findIndexById(Long id) {
        return OptionalInt.of(
                IntStream.range(0, movies.size())
                        .filter(i -> Objects.equals(movies.get(i).getId(), id))
                        .findFirst()
                        .orElseThrow(() ->
                                new NoSuchElementException("Movie with id " + id + " not found"))
        ).getAsInt();
    }
}