package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.command.*;
import ru.spb.miwm64.moviemanager.exceptions.NonExistentParameter;

import java.util.ArrayList;

public class HelpCommand implements Command {
    private final String name = "help";

    @Override
    public ArrayList<Parameter<?>> getParams() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Parameter<?>> getMissingParams() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Parameter<?>> getRemainingRequiredParams() {
        return new ArrayList<>();
    }

    @Override
    public void setParam(Parameter<?> param) {
        throw new NonExistentParameter("Params can not be set in command" + name);
    }

    @Override
    public void setParams(ArrayList<Parameter<?>> params) {
        throw new NonExistentParameter("Params can not be set in command" + name);
    }

    @Override
    public CommandResult execute() {
        CommandResult res = new CommandResultSuccess(getHelp(),getHelp());
        return res;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public String getHelp() {
        return "help - list all command with short synapsis";
    }
}
