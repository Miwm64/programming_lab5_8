package ru.spb.miwm64.moviemanager.client.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.spb.miwm64.moviemanager.client.command.*;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;

import java.util.HashMap;

public final class RegisterCommand extends AbstractCommand {
    private JsonRpcClient jsonRpcClient;
    public RegisterCommand(JsonRpcClient jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;
        this.name = "register";
        this.help = "register - create a new user account (all fields required)";

        // username - String, cannot be null or empty
        Parameter<String> usernameParam = new Parameter<>(
                "username",
                "Enter username",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );

        // password - String, cannot be null or empty (ideally with strength validation)
        Parameter<String> passwordParam = new Parameter<>(
                "password",
                "Enter password",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );

        // email - String, simple non‑empty + basic format check
        Parameter<String> emailParam = new Parameter<>(
                "email",
                "Enter email address",
                s -> s,
                s -> s != null && s.contains("@") && !s.trim().isEmpty(),
                true
        );

        // firstName - String, cannot be null or empty
        Parameter<String> firstNameParam = new Parameter<>(
                "firstName",
                "Enter first name",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );

        // lastName - String, cannot be null or empty
        Parameter<String> lastNameParam = new Parameter<>(
                "lastName",
                "Enter last name",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );

        addParam(usernameParam);
        addParam(passwordParam);
        addParam(emailParam);
        addParam(firstNameParam);
        addParam(lastNameParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();

            String username = getValue("username");
            String password = getValue("password");
            String email = getValue("email");
            String firstName = getValue("firstName");
            String lastName = getValue("lastName");
            HashMap<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("password", password);
            params.put("email", email);
            params.put("firstName", firstName);
            params.put("lastName", lastName);
            String res = jsonRpcClient.call("register", params,
                    new TypeReference<String>() {});
            if (res.equalsIgnoreCase("true")) {
                return new CommandResultFailure("Something went wrong.");
            }

            // On success, optionally return created user info
            return new CommandResultSuccess(
                    res,
                    "Registration successful! You can now log in as " + username + "."
            );
        } catch (Exception e) {
            return new CommandResultFailure(e.getMessage());
        }
    }
}