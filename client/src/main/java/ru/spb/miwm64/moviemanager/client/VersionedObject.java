package ru.spb.miwm64.moviemanager.client;


public class VersionedObject<T> {
    public int id;
    public T data;

    public VersionedObject(int id, T data){
        this.id = id;
        this.data = data;
    }
}
