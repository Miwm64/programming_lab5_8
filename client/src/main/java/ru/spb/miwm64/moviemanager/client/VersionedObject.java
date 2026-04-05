package ru.spb.miwm64.moviemanager.client;

public class VersionedObject<T extends Comparable<? super T>> implements Comparable<VersionedObject<T>> {
    public int version;   // renamed from 'id' to avoid confusion with Movie.id
    public T data;

    public VersionedObject(int version, T data) {
        this.version = version;
        this.data = data;
    }

    @Override
    public int compareTo(VersionedObject<T> other) {
        return this.data.compareTo(other.data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VersionedObject)) return false;
        VersionedObject<?> that = (VersionedObject<?>) obj;
        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}