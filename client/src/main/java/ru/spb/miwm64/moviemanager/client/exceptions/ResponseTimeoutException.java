package ru.spb.miwm64.moviemanager.client.exceptions;

public class ResponseTimeoutException extends NetException {
    public ResponseTimeoutException(String message) {
        super("No response within timeout timing");
    }
}

// TODO timing as input
