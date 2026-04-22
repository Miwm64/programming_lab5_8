package ru.spb.miwm64.moviemanager.client.command;

public class CommandResultFailure implements CommandResult{
    String errorMessage;

    public CommandResultFailure(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public <T> T getDataAs(Class<T> type) {
        return null;
    }
}
