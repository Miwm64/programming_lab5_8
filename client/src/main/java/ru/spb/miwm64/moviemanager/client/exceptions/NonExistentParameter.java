package ru.spb.miwm64.moviemanager.client.exceptions;

public class NonExistentParameter extends RuntimeException {
    public NonExistentParameter(String message) {
        super(message);
    }
}
