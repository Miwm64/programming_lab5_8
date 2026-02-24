package ru.spb.miwm64.moviemanager.command;


public class CommandResultSuccess implements CommandResult{
    private String message;
    private Object data;

    public CommandResultSuccess(Object data, String message) {
        this.data = data;
        this.message = message;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public <T> T getDataAs(Class<T> type) {
        return type.cast(data);
    }
}
