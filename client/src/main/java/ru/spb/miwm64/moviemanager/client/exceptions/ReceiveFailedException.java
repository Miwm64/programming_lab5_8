package ru.spb.miwm64.moviemanager.client.exceptions;

public class ReceiveFailedException extends NetException{
    public ReceiveFailedException(){
        super("Receive failed");
    }
}
