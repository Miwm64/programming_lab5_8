package ru.spb.miwm64.moviemanager.client.exceptions;

public class SerializationException extends RuntimeException {
    public SerializationException() {
        super("Serialization failed");
    }

    public SerializationException(Throwable cause){
        super("Serialization failed", cause);
    }
}
