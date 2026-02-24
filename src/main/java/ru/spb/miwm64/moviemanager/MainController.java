package ru.spb.miwm64.moviemanager;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.command.Command;
import ru.spb.miwm64.moviemanager.command.CommandResult;
import ru.spb.miwm64.moviemanager.commands.AddCommand;
import ru.spb.miwm64.moviemanager.commands.HelpCommand;
import ru.spb.miwm64.moviemanager.commands.ShowCommand;
import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;
import ru.spb.miwm64.moviemanager.exceptions.NonExistentCommand;
import ru.spb.miwm64.moviemanager.io.BufferedFileReader;
import ru.spb.miwm64.moviemanager.io.Reader;
import ru.spb.miwm64.moviemanager.io.Writer;
import ru.spb.miwm64.moviemanager.io.XMLParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class MainController {
    private CollectionManager collectionManager;
    private List<Reader> readers;
    private Reader defaultReader;
    private Writer defaultWriter;
    private Writer writer;
    private XMLParser xmlParser;

    public MainController(CollectionManager collectionManager, Reader defaultReader,
                          Writer defaultWriter, XMLParser xmlParser) {
        this.collectionManager = collectionManager;
        this.readers = new ArrayList<>();
        readers.add(defaultReader);
        this.writer = defaultWriter;
        this.defaultReader = defaultReader;
        this.defaultWriter = defaultWriter;
        this.xmlParser = xmlParser;
    }

    public void run() {
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
            System.out.println("FUCK!");
            System.out.println(e.getStackTrace().toString());
            return;
        }
        // save xml
    }

    private boolean consoleRun() throws IOException {
        try {
            writer.writeln("Enter command:");
            String input = readers.get(0).readNextLine();
            Command cmd;

            ArrayList<String> inputs = new ArrayList<>(Arrays.asList(input.trim().split(" ")));
            switch (inputs.get(0)){
                case "help": {
                    cmd = new HelpCommand();
                    break;
                }
                case "show": {
                    cmd = new ShowCommand(collectionManager);
                    break;
                }
                case "add": {
                    cmd = new AddCommand(collectionManager);
                    break;
                }
                case "exit": {
                    return true;
                }
                case null, default: {
                    throw new NonExistentCommand("Command \"" + inputs.get(0) + "\" does not exist");
                }
            }

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

            Command cmd;
            switch (inputs.get(0)){
                case "help": {
                    cmd = new HelpCommand();
                    break;
                }
                case "show": {
                    cmd = new ShowCommand(collectionManager);
                    break;
                }
                case "add": {
                    cmd = new AddCommand(collectionManager);
                    break;
                }
                case "exit": {
                    return true;
                }
                case null, default: {
                    throw new NonExistentCommand("Command \"" + inputs.get(0) + "\" does not exist");
                }
            }

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
            System.out.println(e.getMessage());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
