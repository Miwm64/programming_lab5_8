package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.command.AbstractCommand;
import ru.spb.miwm64.moviemanager.client.command.CommandResult;
import ru.spb.miwm64.moviemanager.client.command.CommandResultFailure;
import ru.spb.miwm64.moviemanager.client.command.CommandResultSuccess;

public final class ClearCommand extends AbstractCommand {
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
