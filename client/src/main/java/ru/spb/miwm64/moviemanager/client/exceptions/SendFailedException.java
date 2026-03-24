package ru.spb.miwm64.moviemanager.client.exceptions;

public class SendFailedException extends NetException{
    public SendFailedException(){
        super("Send failed");
    }
}
