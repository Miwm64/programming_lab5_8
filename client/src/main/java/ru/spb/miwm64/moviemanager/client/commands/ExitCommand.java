package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.client.command.Command;
import ru.spb.miwm64.moviemanager.client.command.CommandResult;
import ru.spb.miwm64.moviemanager.client.command.Parameter;

import java.util.ArrayList;

public class ExitCommand implements Command {
    @Override
    public ArrayList<Parameter<?>> getParams() {
        return null;
    }

    @Override
    public ArrayList<Parameter<?>> getMissingParams() {
        return null;
    }

    @Override
    public ArrayList<Parameter<?>> getRemainingRequiredParams() {
        return null;
    }

    @Override
    public void setParam(Parameter<?> param) {

    }

    @Override
    public void setParams(ArrayList<Parameter<?>> params) {

    }

    @Override
    public CommandResult execute() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getHelp() {
        return "";
    }
}
