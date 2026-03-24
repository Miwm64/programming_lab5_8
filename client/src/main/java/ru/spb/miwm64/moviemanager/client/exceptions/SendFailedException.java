package ru.spb.miwm64.moviemanager.client.exceptions;

public class SendFailedException extends NetException{
    public SendFailedException(){
        super("Failed to send message");
    }

    public SendFailedException(Throwable cause) {
        super("Failed to send message", cause);
    }
}
