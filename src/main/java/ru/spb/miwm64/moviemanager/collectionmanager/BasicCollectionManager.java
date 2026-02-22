package ru.spb.miwm64.moviemanager.collectionmanager;

import ru.spb.miwm64.moviemanager.entities.Movie;
import ru.spb.miwm64.moviemanager.entities.Person;
import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;

import java.util.ArrayList;
import java.util.Objects;

public class BasicCollectionManager implements CollectionManager {
    ArrayList<Movie> movies;

    public BasicCollectionManager() {
        movies = new ArrayList<>();
    }

    public BasicCollectionManager(ArrayList<Movie> movies) {
        this.movies = new ArrayList<>(movies);
    }

    @Override
    public void append(Movie movie) {
        movies.add(movie);
    }

    @Override
    public void updateId(Long oldId, Long newId) {
        Movie m = null;
        for (Movie mv : movies){
            if (Objects.equals(mv.getId(), newId)){
                throw new InvalidValueException("Movie id must be unique");
            }
            if (Objects.equals(mv.getId(), oldId)){
                m = mv;
            }
        }
        Objects.requireNonNull(m).setId(newId);
    }

    @Override
    public void addIfMin(Movie movie) {
        movies.add(movie);
    }

    @Override
    public Movie getById(Long id) {
        for (Movie mv : movies){
            if (Objects.equals(mv.getId(), id)){
                return mv;
            }
        }
        return null;
    }

    @Override
    public Movie getByIndex(int index) {
        return movies.get(index);
    }

    @Override
    public ArrayList<Movie> getGreater(Person person) {
        return null; // TODO cmp
    }

    @Override
    public ArrayList<Movie> getAll() {
        return movies;
    }

    @Override
    public void removeById(Long id) {
        int c = 0;
        for (Movie mv : movies){
            if (Objects.equals(mv.getId(), id)){
                 movies.remove(c);
            }
            ++c;
        }
    }

    @Override
    public void removeByIndex(int index) {
        movies.remove(index);
    }

    @Override
    public void removeGreater(Movie movie) {
        return; // TODO cmp
    }

    @Override
    public void removeAll() {
        this.movies.clear();
    }

}
