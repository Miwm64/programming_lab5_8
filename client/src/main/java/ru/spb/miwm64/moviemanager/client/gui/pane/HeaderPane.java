package com.example.pane;

import com.example.util.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class HeaderPane extends StackPane {
    private final Label title;
    private final HBox menu;

    private final Button closeButton;
    private final Button hideButton;
    private final Button fullscreenButton;

    private boolean isFullscreen = false;

    public HeaderPane(Stage primaryStage) {
        // top bar
        menu = new HBox();
        closeButton = new Button("X");
        fullscreenButton = new Button("[]");
        hideButton = new Button("-");
        // title
        title = new Label("Movie manager");

        elementInit(primaryStage);

        menu.getChildren().addAll(closeButton, fullscreenButton, hideButton);
//        this.getChildren().add(menu);
        this.getChildren().add(title);
        VBox.setVgrow(this, Priority.ALWAYS); // For vertical resizing
        HBox.setHgrow(this, Priority.ALWAYS); // For horizontal resizing
    }

    private void elementInit(Stage primaryStage) {
        title.setStyle("-fx-background-color: #DAC0A7; -fx-background-radius: 20px;");
        title.setTextFill(Color.web("#A100FF"));
        title.setFont(Helper.getBoldFont(20));
        title.setPadding(new Insets(0, 100, 0, 100));
        menu.setAlignment(Pos.CENTER_LEFT);

        closeButton.setStyle("-fx-background-color: transparent;");
        fullscreenButton.setStyle("-fx-background-color: transparent;");
        hideButton.setStyle("-fx-background-color: transparent;");
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
