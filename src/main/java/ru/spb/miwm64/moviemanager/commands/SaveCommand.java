package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.command.*;
import ru.spb.miwm64.moviemanager.io.ConsoleWriter;
import ru.spb.miwm64.moviemanager.io.FileWriter;
import ru.spb.miwm64.moviemanager.io.Writer;
import ru.spb.miwm64.moviemanager.io.XMLParser;

public final class SaveCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    private XMLParser xmlParser;

    public SaveCommand(CollectionManager collectionManager, XMLParser xmlParser) {
        this.collectionManager = collectionManager;
        this.xmlParser = xmlParser;

        this.name = "save";
        this.help = "save - saves collection into file specified by environment variable";

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
            Writer writer = new FileWriter(getValue("filepath"));
            String xml = xmlParser.parseCollectionIntoXML(collectionManager.getAll());
            writer.write(xml);
            writer.close();
            return new CommandResultSuccess(xml, "File saved successfully");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new CommandResultFailure(e.getMessage());
        }
    }
}
