package ru.spb.miwm64.moviemanager;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.command.Command;
import ru.spb.miwm64.moviemanager.command.CommandFactory;
import ru.spb.miwm64.moviemanager.command.CommandResult;
import ru.spb.miwm64.moviemanager.commands.*;
import ru.spb.miwm64.moviemanager.entities.Movie;
import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;
import ru.spb.miwm64.moviemanager.exceptions.NonExistentCommand;
import ru.spb.miwm64.moviemanager.io.BufferedFileReader;
import ru.spb.miwm64.moviemanager.io.Reader;
import ru.spb.miwm64.moviemanager.io.Writer;
import ru.spb.miwm64.moviemanager.io.XMLParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class MainController {
    final String ENV_VARIABLE = "XML_LOAD";

    private CollectionManager collectionManager;
    private List<Reader> readers;
    private Reader defaultReader;
    private Writer defaultWriter;
    private Writer writer;
    private XMLParser xmlParser;
    private CommandFactory commandFactory;

    public MainController(CollectionManager collectionManager, Reader defaultReader,
                          Writer defaultWriter, XMLParser xmlParser) {
        this.collectionManager = collectionManager;
        this.readers = new ArrayList<>();
        readers.add(defaultReader);
        this.writer = defaultWriter;
        this.defaultReader = defaultReader;
        this.defaultWriter = defaultWriter;
        this.xmlParser = xmlParser;
        this.commandFactory = new CommandFactory(collectionManager, xmlParser);
    }

    public void run() {
        loadCollection();
        try {
            readers.add(0, new BufferedFileReader("input.txt"));
            while (true) {
                boolean result;
                if (readers.get(0) instanceof BufferedFileReader) {
                    result = fileRun();
                } else {
                    result = consoleRun();
                }

                if (result) {
                    break;
                }
            }
        }
        catch (Exception e) {
            return;
        }
        saveCollection();
    }

    private boolean consoleRun() throws IOException {
        try {
            writer.writeln("Enter command:");
            String input = readers.get(0).readNextLine();

            ArrayList<String> inputs = new ArrayList<>(Arrays.asList(input.trim().split(" ")));
            Command cmd = commandFactory.newCommand(inputs.get(0).trim());

            var params = cmd.getParams();

            if (params.size() == 1 && inputs.size() >= 2) {
                params.get(0).fromString(inputs.get(1));
            }
            else {
                int i = 0;
                while (i != params.size()) {
                    try {
                        var param = params.get(i);
                        writer.writeln(param.getPrompt() + ":");
                        input = readers.get(0).readNextLine();
                        if (Objects.equals(input.trim(), "abort")){
                            return false;
                        }
                        param.fromString(input);
                        cmd.setParam(param);
                        ++i;
                        if (Objects.equals(param.getName(), "operatorName") && !param.isSet()){
                            break;
                        }
                    }
                    catch (Exception e){
                        writer.writeln("error: " + e.getMessage());
                    }
                }
            }
            CommandResult res = cmd.execute();
            writer.writeln(res.getMessage());

        }
        catch (RuntimeException e){
            writer.writeln("error: " + e.getMessage());
        }
        catch (IOException e) {
            readers.clear();
            readers.add(defaultReader);
            writer = defaultWriter;
        }


        return false;
    }

    private boolean fileRun() {
        if (!readers.get(0).hasNextLine()) {
            readers.remove(0);
            return false;
        }
        try {
            String input = readers.get(0).readNextLine();
            ArrayList<String> inputs = new ArrayList<>(
                    Arrays.asList(input.trim().split(" ", 2))
            );

            Command cmd = commandFactory.newCommand(inputs.get(0).trim());

            var params = cmd.getParams();
            if (!params.isEmpty()) {
                var givenParams = xmlParser.parse(inputs.get(1));
                for (var param : params) {
                    if (givenParams.containsKey(param.getName())){
                        param.fromString(givenParams.get(param.getName()));
                    }
                    if (Objects.equals(param.getName(), "operatorName") && !param.isSet()){
                        break;
                    }
                }
            }
            cmd.setParams(params);
            cmd.execute();
        }
        catch (IOException e) {
            readers.clear();
            readers.add(defaultReader);
            writer = defaultWriter;
        }
        catch (InvalidValueException e){
            System.out.println(e.getMessage()); // TODO
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void loadCollection() {
        Command cmd = new LoadCommand(collectionManager, xmlParser);
        var filepathParam = cmd.getParams().get(0);
        filepathParam.fromString(System.getenv(ENV_VARIABLE));
        cmd.setParams(new ArrayList<>(List.of(filepathParam)));
        try {
            writer.writeln(cmd.execute().getMessage());
        }
        catch (Exception e){
            return;
        }
    }

    private void saveCollection() {
        Command cmd = new SaveCommand(collectionManager, xmlParser);
        var filepathParam = cmd.getParams().get(0);
        filepathParam.fromString(System.getenv(ENV_VARIABLE));
        cmd.setParams(new ArrayList<>(List.of(filepathParam)));
        try {
            writer.writeln(cmd.execute().getMessage());
        }
        catch (Exception e){
            return;
        }
    }
}
