package ru.spb.miwm64.moviemanager.client.collectionmanager;

import javafx.collections.ObservableList;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

import java.util.List;

public interface ObservableCollection {
    ObservableList<VersionedObject<Movie>> getRawAll();
}
