package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.command.AbstractCommand;
import ru.spb.miwm64.moviemanager.command.CommandResult;
import ru.spb.miwm64.moviemanager.command.CommandResultFailure;
import ru.spb.miwm64.moviemanager.command.CommandResultSuccess;

public class ClearCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public ClearCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;

        this.name = "clear";
        this.help = "clear - clear collection";
    }

    @Override
    public CommandResult execute() {
        try {
            collectionManager.removeAll();

            return new CommandResultSuccess(null, "Cleared successfully");

        } catch (Exception e) {
            return new CommandResultFailure("Failed to get collection info: " + e.getMessage());
        }
    }
}
