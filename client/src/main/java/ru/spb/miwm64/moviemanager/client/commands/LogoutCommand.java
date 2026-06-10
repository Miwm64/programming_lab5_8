package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.collectionmanager.SortedCollectionManager;
import ru.spb.miwm64.moviemanager.client.command.AbstractCommand;
import ru.spb.miwm64.moviemanager.client.command.CommandResult;
import ru.spb.miwm64.moviemanager.client.command.CommandResultFailure;
import ru.spb.miwm64.moviemanager.client.command.CommandResultSuccess;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;

public final class LogoutCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    public LogoutCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.name = "logout";
        this.help = "logout - leave account";
    }

    @Override
    public CommandResult execute() {
        try {
            JsonRpcRequest.token = null;
            JsonRpcRequest.userId = null;
            collectionManager.clear();
            return new CommandResultSuccess(null, "Successfully logged out");

        } catch (Exception e) {
            return new CommandResultFailure("Failed to logout: " + e.getMessage());
        }
    }
}

