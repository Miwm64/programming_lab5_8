package ru.spb.miwm64.moviemanager.command;

import java.util.ArrayList;

public interface Command {
    ArrayList<Parameter<?>> getParams();
    void setParam(Parameter<?> param);
    void setParams(ArrayList<Parameter<?>> params);
    CommandResult execute();
}
