package ru.spb.miwm64.moviemanager.exceptions;

public class NotEnoughParameters extends RuntimeException {
    public NotEnoughParameters(String message) {
        super(message);
    }
}
