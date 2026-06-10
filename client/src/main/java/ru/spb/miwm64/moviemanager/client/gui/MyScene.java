package ru.spb.miwm64.moviemanager.client.gui;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.*;
import ru.spb.miwm64.moviemanager.client.collectionmanager.BatchRemoteCollectionManager;
import ru.spb.miwm64.moviemanager.client.collectionmanager.ObservableCollection;
import ru.spb.miwm64.moviemanager.client.command.Command;
import ru.spb.miwm64.moviemanager.client.command.CommandFactory;
import ru.spb.miwm64.moviemanager.client.commands.LogoutCommand;
import ru.spb.miwm64.moviemanager.client.gui.pane.*;
import ru.spb.miwm64.moviemanager.client.gui.util.GuiFactory;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import ru.spb.miwm64.moviemanager.client.gui.widgets.FooterLabel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.io.Reader;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.common.net.Ownership;

import java.util.*;

public class MyScene extends Scene {
    private final Stage primaryStage;
    private final BorderPane mainPane;

    private final HeaderPane headerPane;
    private final LoginPane loginPane;
    private final RegisterPane registerPane;
    private final FooterLabel footerLabel;
    private final TablePane tablePane;
    private final MapPane mapPane;

    private final List<Reader> readers;
    private Set<String> openedFilesSet;

    private final CommandFactory commandFactory;
    private final Label syncLabel;

    private int footerClickedCount = 0;

    private boolean isTable = true;

    public MyScene(Stage primaryStage, BatchRemoteCollectionManager collectionManager, XMLParser xmlParser,
                   JsonRpcClient jsonRpcClient, ObservableList<Ownership> ownerships) {
        super(new Label(I18N.get("my_scene.label.loading")));
        this.readers = new LinkedList<>();
        this.openedFilesSet = new HashSet<>();
        this.commandFactory = new CommandFactory(collectionManager, xmlParser, readers, openedFilesSet, jsonRpcClient);

        this.primaryStage = primaryStage;
        this.mainPane = new BorderPane();
        this.loginPane = new LoginPane();
        this.registerPane = new RegisterPane();
        this.tablePane = new TablePane(collectionManager, collectionManager, ownerships, jsonRpcClient,
                readers, openedFilesSet, commandFactory);
        this.mapPane = new MapPane(collectionManager, collectionManager, ownerships);
        this.headerPane = new HeaderPane(primaryStage);
        this.footerLabel = new FooterLabel();
        this.syncLabel = new Label();
        this.syncLabel.textProperty().bind(I18N.createBinding("footer.label.sync_label.true"));

        footerLabel.setOnMouseClicked(event -> {
            footerClickedCount++;
            if (footerClickedCount >= 3) {
                footerClickedCount = -1;
                mainPane.setCenter(new EasterEggPane());
            }
            if  (footerClickedCount == 0) {
                mainPane.setCenter(loginPane);
            }
        });


        this.setRoot(this.mainPane);
        mainPane.setStyle("-fx-background-color: #EEDEC5;");
        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(700);

        BorderPane.setMargin(tablePane, new Insets(10, 10, 10, 10));
        this.mainPane.setCenter(loginPane);
        this.mainPane.setTop(headerPane);

        StackPane bottomStack = new StackPane();
        StackPane.setAlignment(syncLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(footerLabel, Pos.CENTER);
        bottomStack.getChildren().addAll(syncLabel, footerLabel);
        this.mainPane.setBottom(bottomStack);

        loginPane.getSwitchButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {mainPane.setCenter(registerPane);});
        registerPane.getSwitchButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {mainPane.setCenter(loginPane);});
        loginPane.getLoginButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            var data = loginPane.getData();
            login(data, false);
        });
        registerPane.getRegisterButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            var data = registerPane.getData();
            register(data);
        });

        headerPane.getSwitchButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (isTable){
                mainPane.setCenter(mapPane);
                headerPane.getSwitchButton().setText("Switch to table");
            }
            else {
                mainPane.setCenter(tablePane);
                headerPane.getSwitchButton().setText("Switch to map");
            }
            isTable = !isTable;
        });
        headerPane.hideTop();
        syncLabel.setVisible(false);
        headerPane.getLogoutButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Command logoutcommand = new LogoutCommand(collectionManager);
            logoutcommand.execute();
            mainPane.setCenter(loginPane);
            headerPane.hideTop();
            syncLabel.setVisible(false);
        });
    }

    private void login(Map<String, String> data, boolean isReg) {
        try {
            Command command = commandFactory.newCommand("login");

            for (var param : command.getParams()) {
                param.fromString(data.get(param.getName()));
                command.setParam(param);
            }
            var res = command.execute();
            if (res.isSuccess()){
                mainPane.setCenter(tablePane);
                isTable = true;
                headerPane.getSwitchButton().setText("Switch to map");
                headerPane.setNickname(data.get("username"));
                headerPane.showTop();
                syncLabel.setVisible(true);
            }
            else{
                if (res.getMessage().contains("UDP")) {
                    GuiFactory.createErrorPopup("Server unavailable, try later").show();
                }
                else{
                    if (isReg){
                        GuiFactory.createErrorPopup("User with this name/email already exists").show();
                    }
                    else{
                        GuiFactory.createErrorPopup("Wrong credentials").show();

                    }
                }
            }
        }
        catch (Exception e) {
            GuiFactory.createErrorPopup("Internal server error").show();
        }
    }

    private void register(Map<String, String> data) {
        try {
            Command command = commandFactory.newCommand("register");
            for (var param : command.getParams()) {
                param.fromString(data.get(param.getName()));
                command.setParam(param);
            }
            var res = command.execute();
            if (res.isSuccess()){
                login(data, true);
            }
            else{
                if (res.getMessage().contains("UDP")) {
                    GuiFactory.createErrorPopup("Server unavailable, try later").show();
                }
                else{
                    GuiFactory.createErrorPopup("User with this name/email already exists").show();
                }
            }
        }
        catch (Exception e) {
            GuiFactory.createErrorPopup("Internal server error").show();
        }
    }

    public StringProperty getSyncProperty() {
        return syncLabel.textProperty();
    }
}
