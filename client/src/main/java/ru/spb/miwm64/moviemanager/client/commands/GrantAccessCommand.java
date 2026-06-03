package ru.spb.miwm64.moviemanager.client.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.spb.miwm64.moviemanager.client.command.*;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;

import java.util.HashMap;
import java.util.Map;

public final class GrantAccessCommand extends AbstractCommand {
    private final JsonRpcClient jsonRpcClient;

    public GrantAccessCommand(JsonRpcClient jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;


        this.name = "grant_access";
        this.help = "grant_access <movieId> <nickname> - grant edit access to another user for a movie you own";

        Parameter<Long> movieIdParam = new Parameter<>(
                "movieId",
                "Enter movie ID",
                Long::parseLong,
                id -> id > 0,
                true
        );

        Parameter<String> nicknameParam = new Parameter<>(
                "nickname",
                "Enter target user nickname",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );

        addParam(movieIdParam);
        addParam(nicknameParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();

            Long movieId = getValue("movieId");
            String nickname = getValue("nickname");

            Map<String, Object> params = new HashMap<>();
            params.put("movieId", movieId);
            params.put("nickname", nickname);

            Boolean result = jsonRpcClient.call(
                    "grantAccess",
                    params,
                    new TypeReference<Boolean>() {}
            );


            return new CommandResultSuccess(result, "Granted edit access to " + nickname);
        } catch (Exception e) {
            return new CommandResultFailure(e.getMessage());
        }
    }
}