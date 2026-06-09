package ru.spb.miwm64.moviemanager.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.client.collectionmanager.BatchRemoteCollectionManager;
import ru.spb.miwm64.moviemanager.client.command.CommandFactory;
import ru.spb.miwm64.moviemanager.client.gui.MyScene;
import ru.spb.miwm64.moviemanager.client.io.ConsoleReader;
import ru.spb.miwm64.moviemanager.client.io.ConsoleWriter;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.client.net.UDPClient;
import ru.spb.miwm64.moviemanager.common.io.Reader;
import ru.spb.miwm64.moviemanager.common.io.Writer;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.client.sync.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("JavaFX application started");
        UDPClient udpClient = new UDPClient(new InetSocketAddress("localhost", 7878));
        JsonRpcClient jsonRpcClient = new JsonRpcClient(udpClient);

        PendingChangeQueue queue = new PendingChangeQueue();
        BatchRemoteCollectionManager collectionManager = new BatchRemoteCollectionManager(queue);

        XMLParser xmlParser = new XMLParser();
        List<String> messages = Collections.synchronizedList(new ArrayList<String>());

        Scene scene = new MyScene(primaryStage, collectionManager, xmlParser, jsonRpcClient);
        stageInit(primaryStage, scene);

        SynchronizationThread thread = new SynchronizationThread(jsonRpcClient, queue, collectionManager,
                FXCollections.observableArrayList(messages));
        thread.start();

        return;
    }

    public void stageInit(Stage primaryStage, Scene scene) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setTitle("Movie manager");
        primaryStage.setWidth(primaryScreenBounds.getWidth()/1.5);
        primaryStage.setHeight(primaryScreenBounds.getHeight()/1.5);
//        primaryStage.initStyle(StageStyle.UNDECORATED);

        // scene creation
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}