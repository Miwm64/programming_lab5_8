package ru.spb.miwm64.moviemanager.commands;

import java.util.ArrayList;

public interface Command {
    Parameter<?> getParams();
    void setParam(Parameter<?> param);
    void setParams(ArrayList<Parameter<?>> params);
    CommandResult execute();
}
