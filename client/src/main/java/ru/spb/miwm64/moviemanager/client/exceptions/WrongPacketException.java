package ru.spb.miwm64.moviemanager.client.exceptions;

public class WrongPacketException extends NetException{
    public WrongPacketException(){
        super("Wrong packet arrived");
    }

    public WrongPacketException(Throwable cause) {
        super("Wrong packet arrived", cause);
    }
}
