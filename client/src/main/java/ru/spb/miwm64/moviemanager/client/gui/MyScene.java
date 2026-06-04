package ru.spb.miwm64.moviemanager.client.gui;

import javafx.scene.control.Button;
import ru.spb.miwm64.moviemanager.client.gui.pane.*;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import ru.spb.miwm64.moviemanager.client.gui.widgets.FooterLabel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class MyScene extends Scene {
    private final BorderPane mainPane;

    private final HeaderPane headerPane;
    private final LoginPane loginPane;
    private final RegisterPane registerPane;
    private final FooterLabel footerLabel;
    private final TablePane tablePane;

    private int footerClickedCount = 0;

    public MyScene(Stage primaryStage) {
        super(new Label("Loading..."));
        this.mainPane = new BorderPane();
        this.loginPane = new LoginPane();
        this.registerPane = new RegisterPane();
        this.tablePane = new TablePane();
        this.headerPane = new HeaderPane(primaryStage);
        this.footerLabel = new FooterLabel();

        Button tmp = new Button();
        tmp.setText("login");
        tmp.setOnMouseClicked((event) -> {
            I18N.setLocale(new  Locale("fr", "fr"));
        });

        loginPane.getChildren().add(tmp);

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
        this.mainPane.setBottom(footerLabel);
        BorderPane.setAlignment(footerLabel, Pos.CENTER);

        loginPane.getSwitchButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {mainPane.setCenter(registerPane);});
        registerPane.getSwitchButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {mainPane.setCenter(loginPane);});
        loginPane.getLoginButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {mainPane.setCenter(tablePane);});
        registerPane.getRegisterButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {mainPane.setCenter(tablePane);});
    }
}
