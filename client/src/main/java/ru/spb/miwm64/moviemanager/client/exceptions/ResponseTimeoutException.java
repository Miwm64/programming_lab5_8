package ru.spb.miwm64.moviemanager.client.exceptions;

public class ResponseTimeoutException extends RuntimeException {
    public ResponseTimeoutException(String message) {
        super(message);
    }
}
