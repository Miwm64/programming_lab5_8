package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.command.*;

public final class RemoveByIndexCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public RemoveByIndexCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;

        this.name = "remove_at";
        this.help = "remove_at <id> - remove by index";

        // id, can not be null, >0
        Parameter<Integer> indexParam = new Parameter<>(
                "index",
                "Enter index",
                Integer::parseInt,
                s -> s > 0,
                true
        );
        addParam(indexParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();

            collectionManager.removeByIndex(getValue("index"));

            return new CommandResultSuccess(true, "Successfully remove movie with index: " + getValue("index"));

        } catch (Exception e) {
            return new CommandResultFailure("Failed to get collection info: " + e.getMessage());
        }
    }
}
