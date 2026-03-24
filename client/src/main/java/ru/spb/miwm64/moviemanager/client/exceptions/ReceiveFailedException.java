package ru.spb.miwm64.moviemanager.client.exceptions;

public class ReceiveFailedException extends NetException{
    public ReceiveFailedException(){
        super("Failed to receive response");
    }

    public ReceiveFailedException(Throwable cause) {
        super("Failed to receive response", cause);
    }
}
