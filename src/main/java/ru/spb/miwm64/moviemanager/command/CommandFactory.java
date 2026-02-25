package ru.spb.miwm64.moviemanager.command;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.commands.*;
import ru.spb.miwm64.moviemanager.exceptions.NonExistentCommand;
import ru.spb.miwm64.moviemanager.io.XMLParser;

import java.util.*;
import java.util.function.Supplier;

public final class CommandFactory {
    private final Map<String, Supplier<Command>> commandsRegistry = new LinkedHashMap<>();
    private CollectionManager collectionManager;
    private XMLParser xmlParser;

    public CommandFactory(CollectionManager collectionManager, XMLParser xmlParser) {
        this.collectionManager = collectionManager;
        this.xmlParser = xmlParser;

        // Register all commands
        registerCommands();
    }

    private void registerCommands() {
        register("help", () -> (new HelpCommand(this)));

        register("show", () -> new ShowCommand(collectionManager));
        register("info", () -> new InfoCommand(collectionManager));
        register("add", () -> new AddCommand(collectionManager));
        register("add_if_min", () -> new AddIfMinCommand(collectionManager));
        register("update_id", () -> new UpdateByIDCommand(collectionManager));
        register("remove_by_id", () -> new RemoveByIDCommand(collectionManager));
        register("remove_at", () -> new RemoveByIndexCommand(collectionManager));
        register("clear", () -> new ClearCommand(collectionManager));

        register("load", () -> new LoadCommand(collectionManager, xmlParser));
        register("save", () -> new SaveCommand(collectionManager, xmlParser));
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
