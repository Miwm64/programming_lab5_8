package ru.spb.miwm64.moviemanager.client.gui.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import ru.spb.miwm64.moviemanager.client.command.Parameter;
import ru.spb.miwm64.moviemanager.client.commands.AddCommand;
import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import ru.spb.miwm64.moviemanager.client.gui.widgets.TableEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public sealed class MyDialog extends Dialog permits CreateDialog, UpdateDialog{
    protected final DialogPane dialogPane;
    protected final VBox mainPane;
    private final ScrollPane scrollPane;
    protected final Map<String, TextField> fieldMap;
    protected ArrayList<Parameter<?>> parameterList;
    protected final Label titleLabel;

    public MyDialog() {
        dialogPane = this.getDialogPane();
        mainPane = new VBox();
        fieldMap = new HashMap<>();
        scrollPane = new ScrollPane(mainPane);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        dialogPane.setStyle("-fx-background-color: #EEDEC5;");
        scrollPane.setStyle("-fx-background-color: #EEDEC5;");
        mainPane.setStyle("-fx-background-color: #EEDEC5;");
        scrollPane.setFitToWidth(true);
        dialogPane.setContent(scrollPane);
        scrollPane.setContent(mainPane);

        titleLabel = new Label();
        titleLabel.setFont(Helper.getBoldFont(14));
        titleLabel.setStyle("-fx-background-color: #DAC0A7;" +
                "-fx-background-radius: 24px;  -fx-border-radius: 24;");
        titleLabel.setPrefWidth(200);
        titleLabel.setAlignment(Pos.CENTER);
        mainPane.getChildren().add(titleLabel);
        mainPane.setMargin(titleLabel, new Insets(0, 0, 60, 0));
    }

    protected void addField(Parameter<?> parameter) {
        BorderPane borderPane = new BorderPane();
        TextField field = new TextField();
        field.setPromptText(parameter.getName());
        borderPane.setCenter(field);

        field.setMaxWidth(300);
        field.setMaxHeight(200);
        field.setStyle("-fx-background-color: #E7B36F; -fx-background-radius: 24; -fx-prompt-text-fill: black;" +
                "-fx-border-color: E7B36F; -fx-border-width: 1; -fx-border-radius: 24;" +
                "-fx-text-fill: black;");
        field.setFont(Helper.getFont(14));
        borderPane.setMargin(field, new Insets(0, 0, 40, 0));

        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    parameter.fromString(field.getText());
                    field.setStyle("-fx-background-color: #E7B36F; -fx-background-radius: 24;-fx-border-color: E7B36F; -fx-border-width: 1; -fx-border-radius: 24;-fx-text-fill: black; -fx-prompt-text-fill: black;");
                }
                catch (Exception e) {
                    field.setStyle("-fx-background-color: #E7B36F; -fx-background-radius: 24;-fx-border-color: E7B36F; -fx-border-width: 1; -fx-border-radius: 24;-fx-text-fill: red; -fx-prompt-text-fill: red;");
                }
            }
        });
        fieldMap.put(parameter.getName(), field);
        mainPane.getChildren().add(borderPane);

    }
}
