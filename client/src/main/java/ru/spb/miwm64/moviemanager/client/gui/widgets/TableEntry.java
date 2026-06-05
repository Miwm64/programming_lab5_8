package ru.spb.miwm64.moviemanager.client.gui.widgets;

import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Random;

// TODO i18n
public class TableEntry extends HBox {
    private Button editButton = new Button("Edit");
    private Label title = createLabel("Title");
    private Label id = createLabel("1");
    private Label coordinate = createLabel("Coordinates");
    private Label creationDate = createLabel("Creation date");
    private Label oscars = createLabel("Oscars");
    private Label goldenPalm = createLabel("Golden Palm");
    private Label genre = createLabel("Genre");
    private Label MPAA_RATING = createLabel("MPAA Rating");
    private Label person =  createLabel("Person");
    private Button deleteButton = new Button("Delete");

    public TableEntry(boolean showButtons) {
        Random random = new Random();
        this.getChildren().add(editButton);
        this.getChildren().add(id);
        this.getChildren().add(title);
        this.getChildren().add(coordinate);
        this.getChildren().add(creationDate);
        this.getChildren().add(oscars);
        this.getChildren().add(goldenPalm);
        this.getChildren().add(genre);
        this.getChildren().add(MPAA_RATING);
        this.getChildren().add(person);
        this.getChildren().add(deleteButton);
        if (!showButtons) {
            editButton.setVisible(false);
            id.setVisible(false);
            deleteButton.setVisible(false);
        }
        HBox.setHgrow(title, Priority.ALWAYS);
        HBox.setHgrow(id, Priority.NEVER);
        HBox.setHgrow(coordinate, Priority.ALWAYS);
        HBox.setHgrow(creationDate, Priority.ALWAYS);
        HBox.setHgrow(oscars, Priority.ALWAYS);
        HBox.setHgrow(goldenPalm, Priority.ALWAYS);
        HBox.setHgrow(genre, Priority.ALWAYS);
        HBox.setHgrow(MPAA_RATING, Priority.ALWAYS);
        HBox.setHgrow(person, Priority.ALWAYS);

        id.setMinWidth(30);
        HBox.setMargin(editButton, new Insets(0, 10, 0, 0));
    }

    private static Label createLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(300);
        label.setMaxHeight(200);
        label.setStyle("-fx-background-color: #EEDEC5; -fx-border-color: black;");
        label.setFont(Helper.getFont(14));
        label.setAlignment(Pos.CENTER);

        setMargin(label, new Insets(0, 10, 0, 0));
        return label;
    }
}
