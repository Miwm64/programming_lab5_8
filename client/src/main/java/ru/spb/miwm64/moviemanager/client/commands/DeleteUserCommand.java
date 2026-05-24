package ru.spb.miwm64.moviemanager.client.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.spb.miwm64.moviemanager.client.command.*;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;

import java.util.HashMap;
import java.util.Objects;

public final class DeleteUserCommand extends AbstractCommand {
    JsonRpcClient jsonRpcClient;
    public DeleteUserCommand(JsonRpcClient jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;

        this.name = "delete_user";
        this.help = "delete_user - permanently delete current user";

        // username - String, cannot be null or empty
        Parameter<String> confirmationParam = new Parameter<>(
                "confirmation",
                "Are you sure? (print yes, if you are)",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );
        addParam(confirmationParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();

            String confirmation = getValue("confirmation");
            if (!Objects.equals(confirmation, "yes")) {
                return new CommandResultSuccess(false, "User not deleted");
            }
            Boolean token = jsonRpcClient.call("deleteUser", null, new TypeReference<Boolean>(){});
            JsonRpcRequest.token = null;

            return new CommandResultSuccess(
                    token,
                    "User deleted!"
            );
        } catch (Exception e) {
            return new CommandResultFailure(e.getMessage());
        }
    }
}