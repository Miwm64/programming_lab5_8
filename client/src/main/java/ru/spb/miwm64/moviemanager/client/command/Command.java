package ru.spb.miwm64.moviemanager.client.command;

import java.util.ArrayList;

public interface Command {
    ArrayList<Parameter<?>> getParams();
    ArrayList<Parameter<?>> getMissingParams();
    ArrayList<Parameter<?>> getRemainingRequiredParams();
    void setParam(Parameter<?> param);
    void setParams(ArrayList<Parameter<?>> params);
    CommandResult execute();

    String getName();
    String getHelp();
}
