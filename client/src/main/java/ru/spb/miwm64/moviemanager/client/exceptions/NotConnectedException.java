package ru.spb.miwm64.moviemanager.client.exceptions;

public class NotConnectedException extends NetException {
    public NotConnectedException() {
        super("UDP client is not connected");
    }
}
