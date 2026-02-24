package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.command.*;
import ru.spb.miwm64.moviemanager.entities.Movie;
import ru.spb.miwm64.moviemanager.io.*;

import java.util.ArrayList;

public final class LoadCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    private XMLParser xmlParser;

    public LoadCommand(CollectionManager collectionManager, XMLParser xmlParser) {
        this.collectionManager = collectionManager;
        this.xmlParser = xmlParser;

        this.name = "load";
        this.help = "load - loads collection from file specified by environment variable. Clears old collection";

        Parameter<String> filepathParam = new Parameter<>(
                "filepath",
                "Enter filepath",
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
            Reader reader = new SimpleFileReader(getValue("filepath"));
            String xml = reader.readNextLine();
            ArrayList<Movie> movies = xmlParser.parseFromXMLCollection(xml);
            collectionManager.setCollection(movies);
            reader.close();
            return new CommandResultSuccess(xml, "File loaded successfully");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new CommandResultFailure(e.getMessage());
        }
    }
}
