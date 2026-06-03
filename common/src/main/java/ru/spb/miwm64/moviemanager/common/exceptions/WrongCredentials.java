package ru.spb.miwm64.moviemanager.common.exceptions;

public class WrongCredentials extends RuntimeException {
    public WrongCredentials() {
        super("Wrong credentials");
    }
}
