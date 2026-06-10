package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.client.command.*;
import ru.spb.miwm64.moviemanager.client.io.FullBufferedFileReader;
import ru.spb.miwm64.moviemanager.common.io.Reader;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;

import java.io.IOException;
import java.util.*;

public class ExecuteScriptCommand extends AbstractCommand {
    private final List<Reader> readers;
    private final Set<String> openedFilesSet;
    private final CommandFactory commandFactory;
    private final XMLParser xmlParser;
    private final CollectionManager collectionManager;
    private final JsonRpcClient jsonRpcClient;

    public ExecuteScriptCommand(List<Reader> readers,
                                Set<String> openedFilesSet,
                                CollectionManager collectionManager,
                                XMLParser xmlParser,
                                JsonRpcClient jsonRpcClient) {
        this.name = "execute_script";
        this.help = "execute_script <filepath> - executes commands from script file";
        this.readers = readers;
        this.openedFilesSet = openedFilesSet;
        this.collectionManager = collectionManager;
        this.xmlParser = xmlParser;
        this.jsonRpcClient = jsonRpcClient;

        // Recreate command factory with the same dependencies
        this.commandFactory = new CommandFactory(
                collectionManager, xmlParser, readers, openedFilesSet, jsonRpcClient
        );

        Parameter<String> filepathParam = new Parameter<>(
                "filepath",
                "Enter script file path:",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );
        addParam(filepathParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();
            String filepath = getValue("filepath");

            // Recursion check
            if (openedFilesSet.contains(filepath)) {
                return new CommandResultFailure(
                        "Recursion detected! File '" + filepath + "' is already being executed"
                );
            }

            openedFilesSet.add(filepath);

            try (Reader scriptReader = new FullBufferedFileReader(filepath)) {
                List<String> results = new ArrayList<>();
                int lineNumber = 0;
                boolean hasErrors = false;

                while (scriptReader.hasNextLine()) {
                    String line = scriptReader.readNextLine();
                    lineNumber++;
                    if (line == null || line.trim().isEmpty()) continue;

                    // Split into command name and the rest (the XML‑encoded parameters)
                    String[] parts = line.trim().split("\\s+", 2);
                    String cmdName = parts[0];
                    String paramString = (parts.length > 1) ? parts[1] : "";

                    // Special handling for exit – stop script execution
                    if ("exit".equalsIgnoreCase(cmdName)) {
                        break;
                    }

                    // Create command instance
                    Command cmd = commandFactory.newCommand(cmdName);
                    if (cmd == null) {
                        hasErrors = true;
                        continue;
                    }

                    // Parse parameters from the XML string
                    Map<String, String> givenParams = xmlParser.parse(paramString);
                    ArrayList<Parameter<?>> params = cmd.getParams();

                    for (Parameter<?> param : params) {
                        String paramName = param.getName();
                        if (givenParams.containsKey(paramName)) {
                            param.fromString(givenParams.get(paramName));
                        }
                        // Special break condition from original MainController
                        if ("operatorName".equals(paramName) && !param.isSet()) {
                            break;
                        }
                    }
                    cmd.setParams(params);

                    // Execute the command
                    CommandResult res = cmd.execute();
                    if (!res.isSuccess()) {
                        hasErrors = true;
                        // Optionally stop on first error? Here we continue.
                    }
                }

                if (hasErrors) {
                    return new CommandResultFailure(
                            "Script finished with errors: " + filepath
                    );
                } else {
                    return new CommandResultSuccess(
                            null,
                            "Script executed successfully: " + filepath
                    );
                }

            } catch (IOException e) {
                return new CommandResultFailure(
                        "Failed to read script file '" + filepath + "': " + e.getMessage()
                );
            } finally {
                openedFilesSet.remove(filepath);
            }

        } catch (Exception e) {
            return new CommandResultFailure("Execute script failed: " + e.getMessage());
        }
    }
}