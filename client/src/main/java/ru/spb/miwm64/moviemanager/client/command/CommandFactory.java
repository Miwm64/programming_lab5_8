package ru.spb.miwm64.moviemanager.client.command;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.commands.*;
import ru.spb.miwm64.moviemanager.common.exceptions.NonExistentCommand;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.common.io.Reader;

import java.util.*;
import java.util.function.Supplier;

public final class CommandFactory {
    private final Map<String, Supplier<Command>> commandsRegistry = new LinkedHashMap<>();
    private CollectionManager collectionManager;
    private XMLParser xmlParser;
    private List<Reader> readers;
    private Set<String> openedFilesSet;

    public CommandFactory(CollectionManager collectionManager, XMLParser xmlParser,
                          List<Reader> readers, Set<String> openedFilesSet) {
        this.collectionManager = collectionManager;
        this.xmlParser = xmlParser;
        this.readers = readers;
        this.openedFilesSet = openedFilesSet;

        // Register all commands
        registerCommands();
    }

    private void registerCommands() {
        register("help", () -> (new HelpCommand(this)));

        register("show", () -> new ShowCommand(collectionManager));
        register("info", () -> new InfoCommand(collectionManager));
        register("count_by_golden_palm_count", () ->
                new CountByGoldenPalmCountCommand(collectionManager));
        register("filter_greater_than_operator", () ->
                new FilterGreaterThanOperatorCommand(collectionManager));
        register("print_field_ascending_golden_palm_count",
                () -> new PrintFieldAscendingGoldenPalmCountCommand(collectionManager));
        register("add", () -> new AddCommand(collectionManager));
        register("add_if_min", () -> new AddIfMinCommand(collectionManager));
        register("update_id", () -> new UpdateByIDCommand(collectionManager));
        register("remove_by_id", () -> new RemoveByIDCommand(collectionManager));
        register("remove_at", () -> new RemoveByIndexCommand(collectionManager));
        register("remove_greater", () -> new RemoveGreaterCommand(collectionManager));
        register("clear", () -> new ClearCommand(collectionManager));

        register("execute_script", () -> new ExecuteScriptCommand(readers, openedFilesSet));
    }

    public void register(String commandName, Supplier<Command> creator) {
        commandsRegistry.put(commandName, creator);
    }

    public Command newCommand(String commandName) {
        Supplier<Command> creator = commandsRegistry.get(commandName);
        if (Objects.isNull(creator)) {
            throw new NonExistentCommand("Command '" + commandName + "' does not exist");
        }
        return creator.get();
    }

    public ArrayList<Command> getAllCommands(){
        ArrayList<Command> res = new ArrayList<>();
        for (var creator : commandsRegistry.values()) {
            res.add(creator.get());
        }
        return res;
    }
}
