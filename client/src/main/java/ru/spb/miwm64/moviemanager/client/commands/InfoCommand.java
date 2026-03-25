package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.client.command.AbstractCommand;
import ru.spb.miwm64.moviemanager.client.command.CommandResult;
import ru.spb.miwm64.moviemanager.client.command.CommandResultFailure;
import ru.spb.miwm64.moviemanager.client.command.CommandResultSuccess;

public final class InfoCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public InfoCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;

        this.name = "info";
        this.help = "info - show information about collection";
    }

    @Override
    public CommandResult execute() {
        try {
            String collectionType = collectionManager.getClass().getSimpleName();
            int size = collectionManager.getAll().size();

            StringBuilder info = new StringBuilder();
            info.append("=== Collection Information ===\n");
            info.append(String.format("Type: %s\n", collectionType));
            info.append(String.format("Elements: %d\n", size));


            if (collectionManager instanceof SortedCollectionManager) {
                info.append(String.format("Sorting: sorted\n"));
            }
            else {
                info.append("Sorting: sorted\n");
            }

            return new CommandResultSuccess(info.toString(), info.toString());

        } catch (Exception e) {
            return new CommandResultFailure("Failed to get collection info: " + e.getMessage());
        }
    }
}

