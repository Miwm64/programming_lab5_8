package ru.spb.miwm64.moviemanager.client.gui.dialog;

import javafx.application.Platform;
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
import ru.spb.miwm64.moviemanager.common.entities.Color;
import ru.spb.miwm64.moviemanager.common.entities.Country;
import ru.spb.miwm64.moviemanager.common.entities.MovieGenre;
import ru.spb.miwm64.moviemanager.common.entities.MpaaRating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract sealed class MyDialog extends Dialog permits CreateDialog, UpdateDialog{
    protected final DialogPane dialogPane;
    protected final VBox mainPane;
    private final ScrollPane scrollPane;
    protected final Map<String, Control> fieldMap;
    protected ArrayList<Parameter<?>> parameterList;
    protected final Label titleLabel;

    protected final ButtonType applyButton;
    protected final ButtonType cancelButton;


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
        titleLabel.setStyle("-fx-background-color: #DAC0A7; -fx-background-radius: 24px; -fx-border-radius: 24;");
        titleLabel.setPrefWidth(200);
        titleLabel.setAlignment(Pos.CENTER);
        mainPane.getChildren().add(titleLabel);
        mainPane.setMargin(titleLabel, new Insets(0, 0, 60, 0));

        // Button setup – correct way
        applyButton = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);
        cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(applyButton, cancelButton);

        Platform.runLater(() -> {
            Button applyBtn = (Button) dialogPane.lookupButton(applyButton);
            Button cancelBtn = (Button) dialogPane.lookupButton(cancelButton);
            if (applyBtn != null) {
                applyBtn.setStyle("-fx-background-color: #DAC0A7; -fx-background-radius: 24px; -fx-border-radius: 24;");
                applyBtn.setOnAction(event -> {
                    execute();
                    close();
                });
            }
            if (cancelBtn != null) {
                cancelBtn.setStyle("-fx-background-color: #DAC0A7; -fx-background-radius: 24px; -fx-border-radius: 24;");
                cancelBtn.setOnAction(event -> close());
            }
        });
    }


    protected void addField(Parameter<?> parameter) {
        BorderPane borderPane = new BorderPane();
        Control inputControl;   // can be TextField or ComboBox
        String paramName = parameter.getName();

        // ----- Enum parameters (hardcoded names) -----
        if (paramName.equalsIgnoreCase("genre") ||
                paramName.equalsIgnoreCase("mpaarating") ||
                paramName.equalsIgnoreCase("hairColor") ||
                paramName.equalsIgnoreCase("nationality")) {

            ComboBox<Object> comboBox = new ComboBox<>();
            // Populate with the appropriate enum constants
            if (paramName.equalsIgnoreCase("genre")) {
                comboBox.getItems().addAll(MovieGenre.values());
            } else if (paramName.equalsIgnoreCase("mpaarating")) {
                comboBox.getItems().addAll(MpaaRating.values());
            } else if (paramName.equalsIgnoreCase("hairColor")) {
                comboBox.getItems().addAll(Color.values());
            } else if (paramName.equalsIgnoreCase("nationality")) {
                comboBox.getItems().addAll(Country.values());
            }
            comboBox.setPromptText(parameter.getName());
            comboBox.setMaxWidth(300);
            comboBox.setStyle("-fx-background-color: #E7B36F; -fx-background-radius: 24; " +
                    "-fx-border-color: #E7B36F; -fx-border-width: 1; -fx-border-radius: 24; " +
                    "-fx-text-fill: black;");

            // Listener to update the Parameter when selection changes
            comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    if (newVal == null && !parameter.isRequired()) {
                        parameter.fromString("");
                        comboBox.setStyle(getValidStyle());
                    } else if (newVal != null) {
                        parameter.fromString(newVal.toString());  // enum name
                        comboBox.setStyle(getValidStyle());
                    }
                } catch (Exception e) {
                    comboBox.setStyle(getInvalidStyle());
                }
            });
            inputControl = comboBox;
        }
        // ----- Date parameter (read-only TextField) -----
        else if (paramName.equalsIgnoreCase("releaseDate")) {  // adjust name as needed
            TextField dateField = new TextField();
            dateField.setPromptText(parameter.getName());
            dateField.setEditable(false);          // not changeable
            dateField.setMaxWidth(300);
            dateField.setMaxHeight(200);
            dateField.setStyle("-fx-background-color: #E7B36F; -fx-background-radius: 24; " +
                    "-fx-prompt-text-fill: black; -fx-border-color: #E7B36F; " +
                    "-fx-border-width: 1; -fx-border-radius: 24; -fx-text-fill: black;");
            dateField.setFont(Helper.getFont(14));
            // No focus listener – value will be set programmatically
            inputControl = dateField;
        }
        // ----- Default editable TextField -----
        else {
            TextField field = new TextField();
            field.setPromptText(parameter.getName());
            field.setMaxWidth(300);
            field.setMaxHeight(200);
            field.setStyle("-fx-background-color: #E7B36F; -fx-background-radius: 24; " +
                    "-fx-prompt-text-fill: black; -fx-border-color: #E7B36F; " +
                    "-fx-border-width: 1; -fx-border-radius: 24; -fx-text-fill: black;");
            field.setFont(Helper.getFont(14));

            field.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    if (field.getText().isEmpty() && !parameter.isRequired()) {
                        field.setStyle(getValidStyle());
                    }
                    try {
                        parameter.fromString(field.getText());
                        field.setStyle(getValidStyle());
                    } catch (Exception e) {
                        field.setStyle(getInvalidStyle());
                    }
                }
            });
            inputControl = field;
        }

        // ----- Common layout -----
        borderPane.setRight(inputControl);
        Label paramLabel = new Label(parameter.getName() + ":");
        paramLabel.setFont(Helper.getBoldFont(14));
        borderPane.setLeft(paramLabel);
        borderPane.setMargin(inputControl, new Insets(0, 0, 40, 0));

        fieldMap.put(parameter.getName(), inputControl);
        mainPane.getChildren().add(borderPane);
    }

    // Helper methods for styles (add these to the class)
    private String getValidStyle() {
        return "-fx-background-color: #E7B36F; -fx-background-radius: 24; " +
                "-fx-border-color: #E7B36F; -fx-border-width: 1; -fx-border-radius: 24; " +
                "-fx-text-fill: black; -fx-prompt-text-fill: black;";
    }

    private String getInvalidStyle() {
        return "-fx-background-color: #E7B36F; -fx-background-radius: 24; " +
                "-fx-border-color: #E7B36F; -fx-border-width: 1; -fx-border-radius: 24; " +
                "-fx-text-fill: red; -fx-prompt-text-fill: red;";
    }

    abstract protected void execute();
}
