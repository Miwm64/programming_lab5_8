package ru.spb.miwm64.moviemanager.client.exceptions;

public class NetException extends RuntimeException {
    public NetException(String message) {
        super(message);
    }
    public NetException(String message, Throwable cause) {
        super(message, cause);
    }
}
