package ru.spb.miwm64.moviemanager.client.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.spb.miwm64.moviemanager.client.command.*;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;

import java.util.HashMap;

public final class LoginCommand extends AbstractCommand {
    JsonRpcClient jsonRpcClient;
    public LoginCommand(JsonRpcClient jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;

        this.name = "login";
        this.help = "login - authenticate user with username and password";

        // username - String, cannot be null or empty
        Parameter<String> usernameParam = new Parameter<>(
                "username",
                "Enter username",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );

        // password - String, cannot be null or empty (in real app, consider masking input)
        Parameter<String> passwordParam = new Parameter<>(
                "password",
                "Enter password",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );

        addParam(usernameParam);
        addParam(passwordParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();

            String username = getValue("username");
            String password = getValue("password");
            HashMap<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("password", password);
            String token = jsonRpcClient.call("login", params, new TypeReference<String>(){});
            JsonRpcRequest.token = token;
            if (token != null) {
                return new CommandResultSuccess(
                        token,
                        "Login successful. Welcome, " + username + "!"
                );
            } else {
                return new CommandResultFailure("Invalid username or password.");
            }
        } catch (Exception e) {
            return new CommandResultFailure(e.getMessage());
        }
    }
}