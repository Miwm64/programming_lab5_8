package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.client.command.*;

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
        msg.append("\n=== Available commands ===\n");
        for (var p : commandFactory.getAllCommands()){
            msg.append(p.getHelp()).append("\n");
        }
        CommandResult res = new CommandResultSuccess(msg.toString(), msg.toString());
        return res;
    }

}
