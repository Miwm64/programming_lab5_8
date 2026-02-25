package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.command.*;
import ru.spb.miwm64.moviemanager.io.BufferedFileReader;
import ru.spb.miwm64.moviemanager.io.Reader;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ExecuteScriptCommand extends AbstractCommand {
    private List<Reader> readers;
    private Set<String> openedFilesSet;

    public ExecuteScriptCommand(List<Reader> readers, Set<String> openedFilesSet) {
        this.name="execute_script";
        this.help = "execute_script <filepath> - executes commands from script file";
        this.readers = readers;
        this.openedFilesSet = openedFilesSet;

        Parameter<String> filepathParam = new Parameter<String>(
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

            // Check for recursion
            if (openedFilesSet.contains(filepath)) {
                return new CommandResultFailure(
                        "Recursion detected! File '" + filepath + "' is already being executed"
                );
            }

            // Add to opened files set
            openedFilesSet.add(filepath);

            try {
                Reader scriptReader = new BufferedFileReader(filepath);
                readers.add(0, scriptReader);

                return new CommandResultSuccess(
                        null,
                        "Started executing script: " + filepath
                );

            } catch (IOException e) {
                // Remove from set if file couldn't be opened
                openedFilesSet.remove(filepath);
                return new CommandResultFailure(
                        "Failed to open script file '" + filepath + "': " + e.getMessage()
                );
            }

        } catch (Exception e) {
            return new CommandResultFailure("Execute script failed: " + e.getMessage());
        }
    }
}