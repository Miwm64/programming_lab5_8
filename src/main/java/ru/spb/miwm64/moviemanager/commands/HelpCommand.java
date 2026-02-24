package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.command.*;
import ru.spb.miwm64.moviemanager.exceptions.NonExistentParameter;

import java.util.ArrayList;

public final class HelpCommand extends AbstractCommand  {
    private CommandFactory commandFactory;
    public HelpCommand(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
        this.name = "help";
        this.help = "help - list all command with short synopsis";
    }

    @Override
    public CommandResult execute() {
        StringBuilder msg = new StringBuilder();
        for (var p : commandFactory.getAllCommands()){
            msg.append(p.getHelp()).append("\n");
        }
        CommandResult res = new CommandResultSuccess(msg.toString(), msg.toString());
        return res;
    }

}
