package ru.spb.miwm64.moviemanager.client;

import ru.spb.miwm64.moviemanager.client.commands.AbortCommand;
import ru.spb.miwm64.moviemanager.client.commands.ExitCommand;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public final class MainController {
    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    private List<Reader> readers;
    private Set<String> openedFilesSet;
    private Reader defaultReader;
    private Writer defaultWriter;
    private Writer writer;
    private XMLParser xmlParser;
    private CommandFactory commandFactory;
    private volatile List<String> messages;
    private volatile boolean isInParamInput = false;
    private final Object messageLock = new Object();

    public MainController(CollectionManager collectionManager, Reader defaultReader,
                          Writer defaultWriter, XMLParser xmlParser, List<String> messages) {
        this.readers = new LinkedList<>();
        readers.add(defaultReader);
        this.writer = defaultWriter;
        this.defaultReader = defaultReader;
        this.defaultWriter = defaultWriter;
        this.xmlParser = xmlParser;
        this.openedFilesSet = new HashSet<>();
        this.commandFactory = new CommandFactory(collectionManager, xmlParser, readers, openedFilesSet);
        this.messages = messages;
        LOG.info("MainController initialized");
    }

    public void run() {
        initSyncPrinter();
        try {
            LOG.info("Client main loop started");
            while (true) {
                boolean result;
                if (readers.get(0) instanceof FullBufferedFileReader) {
                    result = fileRun();
                } else {
                    result = consoleRun();
                }

                if (result) {
                    LOG.info("Client exiting main loop");
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error("Unhandled exception in main loop", e);
            return;
        }
    }

    private Command inputCommand() throws IOException {
        writer.writeln("Enter command:");
        String input = readers.get(0).readNextLine();
        LOG.info("Console input: {}", input);

        ArrayList<String> inputs = new ArrayList<>(Arrays.asList(input.trim().split(" ")));

        if (Objects.equals(inputs.get(0), "exit")) {
            LOG.info("Exit command received");
            return new ExitCommand();
        }

        Command cmd = commandFactory.newCommand(inputs.get(0).trim());
        var params = cmd.getParams();
        if (!params.isEmpty() && inputs.size() >= 2) {
            params.get(0).fromString(inputs.get(1));
            cmd.setParam(params.get(0));
        }
        return cmd;
    }

    private Command inputParams(Command cmd) throws IOException {
        var params = cmd.getParams();
        String input;

        int i = 0;
        int skip = 0;
        isInParamInput = true;
        try {
        while (i != params.size()) {
            try {
                if (skip > 0){
                    skip++;
                    i++;
                    continue;
                }
                var param = params.get(i);
                if (param.isSet()) {
                    ++i;
                    continue;
                }

                LOG.debug("Prompting for param: {} ({})", param.getName(), param.getPrompt());
                writer.writeln(param.getPrompt() + ":");
                input = readers.get(0).readNextLine();

                if (Objects.equals(input.trim(), "abort")) {
                    LOG.info("User aborted param input");
                    return new AbortCommand();
                }

                param.fromString(input);
                cmd.setParam(param);
                ++i;
                if (param.isComposite() && !param.isSet()){
                    skip = param.compositeSize()-1;
                }
            } catch (Exception e) {
                LOG.error("Error parsing param input", e);
                writer.writeln("error: " + e.getMessage());
            }
        }
        }
        finally {
            isInParamInput = false;
        }
        return cmd;
    }

    private void executeCommand(Command cmd) throws IOException {
        MDC.put("requestId", UUID.randomUUID().toString());
        try {
            CommandResult res = cmd.execute();
            LOG.info("Command executed: {} → {}", cmd.getClass().getSimpleName(), res.getMessage());
            writer.writeln(res.getMessage());
        } finally {
            MDC.remove("requestId");
        }
    }

    private boolean consoleRun() throws IOException {
        try {
            Command cmd = inputCommand();
            if (cmd instanceof ExitCommand){
                return true;
            }

            cmd = inputParams(cmd);

            if (cmd instanceof AbortCommand){
                return false;
            }

            executeCommand(cmd);
        } catch (RuntimeException e) {
            LOG.error("Runtime exception during consoleRun", e);
            writer.writeln("error: " + e.getMessage());
        } catch (IOException e) {
            LOG.error("IOException, resetting readers/writer", e);
            readers.clear();
            readers.add(defaultReader);
            writer = defaultWriter;
        }

        return false;
    }

    private Command parseParams(ArrayList<String> inputs){
        if (Objects.equals(inputs.get(0), "exit")) {
            LOG.info("Exit command received from file");
            return new ExitCommand();
        }

        Command cmd = commandFactory.newCommand(inputs.get(0).trim());
        LOG.info("Created command from file: {}", cmd.getClass().getSimpleName());

        var params = cmd.getParams();
        if (!params.isEmpty() && inputs.size() >= 2) {
            var givenParams = xmlParser.parse(inputs.get(1));
            for (var param : params) {
                if (givenParams.containsKey(param.getName())) {
                    param.fromString(givenParams.get(param.getName()));
                    LOG.debug("Set param {} = {}", param.getName(), givenParams.get(param.getName()));
                }
                if (Objects.equals(param.getName(), "operatorName") && !param.isSet()) {
                    break;
                }
            }
        }
        cmd.setParams(params);
        return cmd;
    }

    private boolean fileRun() throws IOException {
        if (!checkReader()) {
            return false;
        }

        try {
            String input = readers.get(0).readNextLine();
            LOG.info("File input: {}", input);
            ArrayList<String> inputs = new ArrayList<>(Arrays.asList(input.trim().split(" ", 2)));

            Command cmd = parseParams(inputs);
            if (cmd instanceof ExitCommand){
                return true;
            }

            executeCommand(cmd);
        } catch (IOException e) {
            LOG.error("IOException, resetting readers/writer", e);
            readers.clear();
            readers.add(defaultReader);
            writer = defaultWriter;
        } catch (RuntimeException e) {
            LOG.error("Runtime exception during fileRun", e);
            writer.writeln("error: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected exception during fileRun", e);
            writer.writeln("error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return false;
    }

    private boolean checkReader() {
        if (!readers.get(0).hasNextLine()) {
            FullBufferedFileReader reader = (FullBufferedFileReader) readers.get(0);
            LOG.debug("Closing file reader: {}", reader.getFilepath());
            openedFilesSet.remove(reader.getFilepath());
            readers.remove(0);
            return false;
        }
        return true;
    }

    private void initSyncPrinter() {
        Thread messagePrinter = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
                if (!isInParamInput) {
                    synchronized (messages) {
                        if (!messages.isEmpty()) {
                            try {
                                writer.writeln("");
                                for (String msg : messages) {
                                    writer.writeln(msg);
                                }
                                messages.clear();
                                writer.writeln("Enter command: ");
                            }
                            catch (Exception e){
                                LOG.error("Sync printing error: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        });
        messagePrinter.setDaemon(true);
        messagePrinter.start();
    }
}