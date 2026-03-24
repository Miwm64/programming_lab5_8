package ru.spb.miwm64.moviemanager.client.exceptions;

public class NetError extends RuntimeException {
    public NetError(String message) {
        super(message);
    }
    public NetError(String message, Throwable cause) {
        super(message, cause);
    }
}
