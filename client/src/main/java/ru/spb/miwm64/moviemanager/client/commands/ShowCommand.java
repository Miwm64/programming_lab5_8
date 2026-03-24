package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.client.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.client.command.*;
import ru.spb.miwm64.moviemanager.common.entities.Movie;

import java.util.ArrayList;

public final class ShowCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.name = "show";
        this.help = "show - show all elements of collection";
    }


    @Override
    public CommandResult execute() {
        CommandResult res;
        try {
            ArrayList<Movie> movies = collectionManager.getAll();
            StringBuilder message = new StringBuilder();
            for (var mv : movies) {
                message.append(mv);
                message.append("\n");
            }
            res = new CommandResultSuccess(movies, message.toString());
        }
        catch (Exception e){
            res = new CommandResultFailure(e.getMessage());
        }
        return res;
    }

}
