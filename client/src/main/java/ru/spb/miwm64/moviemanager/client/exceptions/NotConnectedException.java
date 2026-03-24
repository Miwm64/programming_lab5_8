package ru.spb.miwm64.moviemanager.client.exceptions;

public class NotConnectedException extends NetException {
    public NotConnectedException() {
        super("Failed to connect");
    }

    public NotConnectedException(Throwable cause) {
        super("Failed to connect", cause);
    }
}
