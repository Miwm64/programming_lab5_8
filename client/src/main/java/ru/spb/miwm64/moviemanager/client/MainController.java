package ru.spb.miwm64.moviemanager.client;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.command.Command;
import ru.spb.miwm64.moviemanager.client.command.CommandFactory;
import ru.spb.miwm64.moviemanager.client.command.CommandResult;
import ru.spb.miwm64.moviemanager.client.io.FullBufferedFileReader;
import ru.spb.miwm64.moviemanager.common.io.Reader;
import ru.spb.miwm64.moviemanager.common.io.Writer;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;

import java.io.IOException;
import java.util.*;

public final class MainController {
    private CollectionManager collectionManager;
    private List<Reader> readers;
    private Set<String> openedFilesSet;
    private Reader defaultReader;
    private Writer defaultWriter;
    private Writer writer;
    private XMLParser xmlParser;
    private CommandFactory commandFactory;

    public MainController(CollectionManager collectionManager, Reader defaultReader,
                          Writer defaultWriter, XMLParser xmlParser) {
        this.collectionManager = collectionManager;
        this.readers = new LinkedList<>();
        readers.add(defaultReader);
        this.writer = defaultWriter;
        this.defaultReader = defaultReader;
        this.defaultWriter = defaultWriter;
        this.xmlParser = xmlParser;
        this.openedFilesSet = new HashSet<String>();
        this.commandFactory = new CommandFactory(collectionManager, xmlParser, readers, openedFilesSet);
    }

    public void run() {
        try {
            while (true) {
                boolean result;
                if (readers.get(0) instanceof FullBufferedFileReader) {
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
    }

    private boolean consoleRun() throws IOException {
        try {
            writer.writeln("Enter command:");
            String input = readers.get(0).readNextLine();
            ArrayList<String> inputs = new ArrayList<>(Arrays.asList(input.trim().split(" ")));

            if (Objects.equals(inputs.get(0), "exit")){
                return true;
            }

            Command cmd = commandFactory.newCommand(inputs.get(0).trim());
            var params = cmd.getParams();
            if (!params.isEmpty() && inputs.size() >= 2) {
                params.get(0).fromString(inputs.get(1));
                cmd.setParam(params.get(0));
            }


            {
                int i = 0;
                while (i != params.size()) {
                    try {
                        var param = params.get(i);
                        if (param.isSet()){
                            ++i;
                            continue;
                        }

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

    private boolean fileRun() throws IOException {
        if (!checkReader()){
            return false;
        }

        try {
            String input = readers.get(0).readNextLine();
            ArrayList<String> inputs = new ArrayList<>(
                    Arrays.asList(input.trim().split(" ", 2))
            );

            if (Objects.equals(inputs.get(0), "exit")){
                return true;
            }

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
            writer.writeln(cmd.execute().getMessage());
        }
        catch (IOException e) {
            readers.clear();
            readers.add(defaultReader);
            writer = defaultWriter;
        }
        catch (RuntimeException e){
            writer.writeln("error: " + e.getMessage());
        }
        catch (Exception e) {
            writer.writeln("error: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean checkReader() {
        if (!readers.get(0).hasNextLine()) {
            FullBufferedFileReader reader = (FullBufferedFileReader) readers.get(0);
            openedFilesSet.remove(reader.getFilepath());
            readers.remove(0);
            return false;
        }

        return true;
    }
}
