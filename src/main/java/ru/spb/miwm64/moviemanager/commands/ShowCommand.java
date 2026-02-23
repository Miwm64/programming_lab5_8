package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.command.*;
import ru.spb.miwm64.moviemanager.entities.Movie;
import ru.spb.miwm64.moviemanager.exceptions.NonExistentParameter;

import java.util.ArrayList;

public class ShowCommand implements Command {
    private final String name = "show";
    private CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

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
        CommandResult res;
        try {
            ArrayList<Movie> movies = collectionManager.getAll();
            String message = ""+movies.size();
            res = new CommandResultSuccess(movies, message);
        }
        catch (Exception e){
            res = new CommandResultFailure(e.getMessage());
        }
        return res;
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
