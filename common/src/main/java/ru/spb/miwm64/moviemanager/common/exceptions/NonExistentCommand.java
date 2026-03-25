package ru.spb.miwm64.moviemanager.common.exceptions;

public class NonExistentCommand extends RuntimeException {
    public NonExistentCommand(String message) {
        super(message);
    }
}
