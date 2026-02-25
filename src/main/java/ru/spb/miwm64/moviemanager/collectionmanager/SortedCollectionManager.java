package ru.spb.miwm64.moviemanager.collectionmanager;

import ru.spb.miwm64.moviemanager.entities.Movie;
import ru.spb.miwm64.moviemanager.entities.Person;
import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;

import java.util.*;

public class SortedCollectionManager implements CollectionManager {
    private ArrayList<Movie> movies;
    private Long lastAssignedId = 1L;
    private Map<Long, Boolean> currentIDs = new HashMap<>();

    public SortedCollectionManager() {
        movies = new ArrayList<>();
    }

    public SortedCollectionManager(ArrayList<Movie> movies) {
        this.movies = new ArrayList<>(movies);
    }

    @Override
    public void append(Movie movie) {
        Objects.requireNonNull(movie);
        if (!Objects.isNull(movie.getId())) {
            for (Movie mv : movies) {
                if (Objects.equals(mv.getId(), movie.getId())) {
                    throw new InvalidValueException("Movie id must be unique");
                }
            }
        }
        else {
            while (currentIDs.containsKey(lastAssignedId)){
                ++lastAssignedId;
            }
            movie.setId(lastAssignedId);
            currentIDs.put(lastAssignedId, true);
        }

        int index = Collections.binarySearch(movies, movie);

        if (index < 0) {
            index = -index - 1;
        }

        movies.add(index, movie);
    }

    private void updateId(Long oldId, Long newId) {
        if (oldId.equals(newId)) return;

        Movie m = null;
        for (Movie mv : movies){
            if (Objects.equals(mv.getId(), newId)){
                throw new InvalidValueException("Movie id must be unique");
            }
            if (Objects.equals(mv.getId(), oldId)){
                m = mv;
            }
        }

        if (Objects.isNull(m)) {
            throw new NoSuchElementException("Movie with id " + oldId + " not found");
        }

        m.setId(newId);
    }

    @Override
    public boolean addIfMin(Movie movie) {
        if (movies.isEmpty()) {
            append(movie);
            return true;
        }

        if (movie.compareTo(movies.getFirst()) < 0) {
            append(movie);
            return true;
        }
        return false;
    }

    @Override
    public void setCollection(ArrayList<Movie> movies) {
        removeAll();
        this.movies = new ArrayList<>(movies);
        for (var mv : movies){
            currentIDs.put(mv.getId(), true);
        }
    }

    @Override
    public void setById(Long id, Movie movie) {
        for (int i = 0; i < movies.size(); ++i){
            if (Objects.equals(movies.get(i).getId(), id)){
                movies.set(i, movie);
                return;
            }
        }
        throw new NoSuchElementException("Movie with id " + id + " not found");
    }

    @Override
    public Movie getById(Long id) {
        for (Movie mv : movies){
            if (Objects.equals(mv.getId(), id)){
                return mv;
            }
        }
        throw new NoSuchElementException("Movie with id " + id + " not found");
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
        var res = new ArrayList<Movie>();

        for (Movie mv : movies){
            Person operator = mv.getOperator();
            if (Objects.isNull(operator)){
                continue;
            }
            if (operator.compareTo(person) > 0){
                res.add(mv);
            }
        }
        return res;
    }

    @Override
    public ArrayList<Movie> getAll() {
        return new ArrayList<>(movies);
    }

    @Override
    public void removeById(Long id) {
        int c = 0;
        for (Movie mv : movies){
            if (Objects.equals(mv.getId(), id)){
                 movies.remove(c);
                 return;
            }
            ++c;
        }
        throw new NoSuchElementException("Movie with id " + id + " not found");
    }

    @Override
    public void removeByIndex(int index) {
        if (index < 0 || index >= movies.size()){
            throw new InvalidValueException("Index is out of bounds: " + index);
        }
        movies.remove(index);
    }

    @Override
    public void removeGreater(Movie movie) {
        Objects.requireNonNull(movie);
        int index = Collections.binarySearch(movies, movie);

        if (index >= 0) {
            while (index < movies.size() && movies.get(index).compareTo(movie) == 0) {
                ++index;
            }
        }
        else {
            index = -index - 1;
        }

        if (index < movies.size()) {
            movies.subList(index, movies.size()).clear();
        }
    }

    @Override
    public void removeAll() {
        this.movies.clear();
        this.currentIDs.clear();
        this.lastAssignedId = 1L;
    }

}
