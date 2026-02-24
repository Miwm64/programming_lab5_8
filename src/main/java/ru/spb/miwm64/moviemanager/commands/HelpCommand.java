package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.command.*;
import ru.spb.miwm64.moviemanager.exceptions.NonExistentParameter;

import java.util.ArrayList;

public final class HelpCommand extends AbstractCommand  {
    public HelpCommand(){
        this.name = "help";
        this.help = "help - list all command with short synopsis";
    }

    @Override
    public CommandResult execute() {
        CommandResult res = new CommandResultSuccess(getHelp(), getHelp());
        return res;
    }

}
