package ru.spb.miwm64.moviemanager.commands;

import java.util.Objects;

public interface CommandResult {
    boolean isSuccess();
    String getMessage();
    Object getData();
    <T> T getDataAs(Class<T> type);
}
