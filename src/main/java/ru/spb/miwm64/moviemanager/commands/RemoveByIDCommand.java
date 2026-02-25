package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.command.*;

public class RemoveByIDCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public RemoveByIDCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;

        this.name = "remove_at";
        this.help = "remove_at <id> - remove movie with specified id";

        // id, can not be null, >0
        Parameter<Long> idParam = new Parameter<>(
                "id",
                "Enter id",
                Long::parseLong,
                s -> s > 0,
                true
        );
        addParam(idParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();

            collectionManager.removeById(getValue("id"));

            return new CommandResultSuccess(true, "Successfully remove movie with id: " + getValue("id"));

        } catch (Exception e) {
            return new CommandResultFailure("Failed to get collection info: " + e.getMessage());
        }
    }
}
