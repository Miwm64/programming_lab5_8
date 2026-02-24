package ru.spb.miwm64.moviemanager.exceptions;

public class NonExistentParameter extends RuntimeException {
    public NonExistentParameter(String message) {
        super(message);
    }
}
