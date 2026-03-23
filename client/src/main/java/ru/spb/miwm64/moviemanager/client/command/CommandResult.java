package ru.spb.miwm64.moviemanager.client.command;

public interface CommandResult {
    boolean isSuccess();
    String getMessage();
    Object getData();
    <T> T getDataAs(Class<T> type);
}
