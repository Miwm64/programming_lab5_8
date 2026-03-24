package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.command.*;

public final class RemoveByIDCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public RemoveByIDCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;

        this.name = "remove_by_id";
        this.help = "remove_by_id <id> - remove movie with specified id";

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
