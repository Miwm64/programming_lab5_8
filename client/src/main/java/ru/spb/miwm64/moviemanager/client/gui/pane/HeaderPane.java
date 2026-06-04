package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.*;
import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Locale;

public class HeaderPane extends StackPane {
    private final Label title;
    private final HBox menu;

    private final Button closeButton;
    private final Button hideButton;
    private final Button fullscreenButton;

    private boolean isFullscreen = false;
    private BorderPane mainPane;
    private ComboBox<String> changeLocalization;

    public HeaderPane(Stage primaryStage) {
        menu = new HBox();
        closeButton = new Button();
        fullscreenButton = new Button();
        hideButton = new Button();
        title = new Label();
        mainPane = new BorderPane();
        changeLocalization = new ComboBox<>();
        ObservableList<String> langs = FXCollections.observableArrayList();
        langs.add("en");
        langs.add("fr");
        changeLocalization.setItems(langs);
        changeLocalization.getSelectionModel().select(0);
        changeLocalization.valueProperty().addListener((observable, oldValue, newValue) -> {
            I18N.setLocale(new Locale(newValue));
        });

        elementInit(primaryStage);

        menu.getChildren().addAll(closeButton, fullscreenButton, hideButton);
        mainPane.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        mainPane.setRight(changeLocalization);
        this.getChildren().add(mainPane);
        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
    }

    private void elementInit(Stage primaryStage) {
        title.setStyle("-fx-background-color: #DAC0A7; -fx-background-radius: 20px;");
        title.setTextFill(Color.web("#A100FF"));
        title.setFont(Helper.getBoldFont(20));
        title.setPadding(new Insets(0, 100, 0, 100));
        title.textProperty().bind(I18N.createBinding("header_pane.label.title"));

        menu.setAlignment(Pos.CENTER_LEFT);

        closeButton.setStyle("-fx-background-color: transparent;");
        closeButton.textProperty().bind(I18N.createBinding("header_pane.button.close"));

        fullscreenButton.setStyle("-fx-background-color: transparent;");
        fullscreenButton.textProperty().bind(I18N.createBinding("header_pane.button.fullscreen"));

        hideButton.setStyle("-fx-background-color: transparent;");
        hideButton.textProperty().bind(I18N.createBinding("header_pane.button.hide"));

        closeButton.setOnMouseClicked(event -> {
            primaryStage.close();
        });
        fullscreenButton.setOnMouseClicked(event -> {
            if (isFullscreen) {
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                primaryStage.setFullScreen(false);
                primaryStage.setWidth(primaryScreenBounds.getWidth()/1.5);
                primaryStage.setHeight(primaryScreenBounds.getHeight()/1.5);
            }
            else{
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getBounds();
                primaryStage.setFullScreen(true);
                primaryStage.setWidth(primaryScreenBounds.getWidth());
                primaryStage.setHeight(primaryScreenBounds.getHeight());
            }
            isFullscreen = !isFullscreen;
        });
        hideButton.setOnMouseClicked(event -> {
            primaryStage.setIconified(true);
        });
    }
}