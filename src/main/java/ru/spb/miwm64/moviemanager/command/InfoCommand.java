package ru.spb.miwm64.moviemanager.command;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.entities.Movie;
import ru.spb.miwm64.moviemanager.io.Reader;
import ru.spb.miwm64.moviemanager.io.SimpleFileReader;

import java.util.ArrayList;

public class InfoCommand extends AbstractCommand{
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
                info.append("Sorting: unsorted\n");
            }

            return new CommandResultSuccess(info.toString(), info.toString());

        } catch (Exception e) {
            return new CommandResultFailure("Failed to get collection info: " + e.getMessage());
        }
    }
}

