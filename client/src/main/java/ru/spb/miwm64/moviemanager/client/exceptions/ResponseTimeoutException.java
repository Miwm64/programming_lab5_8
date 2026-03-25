package ru.spb.miwm64.moviemanager.client.exceptions;

public class ResponseTimeoutException extends NetException {
    public ResponseTimeoutException() {
        super("No response within timeout timing");
    }
    public ResponseTimeoutException(int timing) {
        super("No response within timeout timing:" + timing);
    }
}

