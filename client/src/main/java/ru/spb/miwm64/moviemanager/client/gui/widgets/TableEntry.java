package ru.spb.miwm64.moviemanager.client.gui.widgets;

import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import ru.spb.miwm64.moviemanager.common.entities.Movie;

import java.time.format.DateTimeFormatter;

// TODO i18n
public class TableEntry extends HBox {
    private Button editButton = new Button("Edit");
    private Label title = createLabel("Title");
    private Label id = createLabel("");
    private Label coordinate = createLabel("Coordinates");
    private Label creationDate = createLabel("Creation date");
    private Label oscars = createLabel("Oscars");
    private Label goldenPalm = createLabel("Golden Palm");
    private Label genre = createLabel("Genre");
    private Label MPAA_RATING = createLabel("MPAA");
    private Label person =  createLabel("Person");
    private Button deleteButton = new Button("Delete");

    private static final DoubleProperty titleWidth = new SimpleDoubleProperty(100);
    private static final DoubleProperty idWidth = new SimpleDoubleProperty(30);
    private static final DoubleProperty coordWidth = new SimpleDoubleProperty(100);
    private static final DoubleProperty dateWidth = new SimpleDoubleProperty(100);
    private static final DoubleProperty oscarsWidth = new SimpleDoubleProperty(60);
    private static final DoubleProperty palmWidth = new SimpleDoubleProperty(80);
    private static final DoubleProperty genreWidth = new SimpleDoubleProperty(80);
    private static final DoubleProperty ratingWidth = new SimpleDoubleProperty(50);
    private static final DoubleProperty personWidth = new SimpleDoubleProperty(100);

    public TableEntry(boolean showButtons) {
        this();
        if (!showButtons) {
            editButton.setVisible(false);
            id.setVisible(false);
            deleteButton.setVisible(false);
        }
    }

    public TableEntry(Movie movie) {
        this();
        setMovie(movie);
    }

    public void setMovie(Movie movie) {
        this.title.setText(movie.getName());
        this.coordinate.setText("(" + movie.getCoordinates().getX() + ", " + movie.getCoordinates().getY() + ")");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy-HH:mm");
        String formattedString = movie.getCreationDate().format(formatter);
        this.creationDate.setText(formattedString);
        this.oscars.setText(""+movie.getOscarsCount());
        this.goldenPalm.setText(""+movie.getGoldenPalmCount());
        this.genre.setText(movie.getGenre().toString());
        this.MPAA_RATING.setText(movie.getMpaaRating().toString());
//        this.person.setText(movie.getOperator().toString());
        updateWidths();
    }

    private TableEntry() {
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

        title.prefWidthProperty().bind(titleWidth);
        id.prefWidthProperty().bind(idWidth);
        coordinate.prefWidthProperty().bind(coordWidth);
        creationDate.prefWidthProperty().bind(dateWidth);
        oscars.prefWidthProperty().bind(oscarsWidth);
        goldenPalm.prefWidthProperty().bind(palmWidth);
        genre.prefWidthProperty().bind(genreWidth);
        MPAA_RATING.prefWidthProperty().bind(ratingWidth);
        person.prefWidthProperty().bind(personWidth);

        HBox.setHgrow(title, Priority.NEVER);
        HBox.setHgrow(id, Priority.NEVER);
        HBox.setHgrow(coordinate, Priority.NEVER);
        HBox.setHgrow(creationDate, Priority.NEVER);
        HBox.setHgrow(oscars, Priority.NEVER);
        HBox.setHgrow(goldenPalm, Priority.NEVER);
        HBox.setHgrow(genre, Priority.NEVER);
        HBox.setHgrow(MPAA_RATING, Priority.NEVER);
        HBox.setHgrow(person, Priority.NEVER);

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

    private void updateWidths() {
        double w = computeWidth(title);
        if (w > titleWidth.get()) titleWidth.set(w);
        w = computeWidth(id);
        if (w > idWidth.get()) idWidth.set(w);
        w = computeWidth(coordinate);
        if (w > coordWidth.get()) coordWidth.set(w);
        w = computeWidth(creationDate);
        if (w > dateWidth.get()) dateWidth.set(w);
        w = computeWidth(oscars);
        if (w > oscarsWidth.get()) oscarsWidth.set(w);
        w = computeWidth(goldenPalm);
        if (w > palmWidth.get()) palmWidth.set(w);
        w = computeWidth(genre);
        if (w > genreWidth.get()) genreWidth.set(w);
        w = computeWidth(MPAA_RATING);
        if (w > ratingWidth.get()) ratingWidth.set(w);
        w = computeWidth(person);
        if (w > personWidth.get()) personWidth.set(w);
    }

    private double computeWidth(Label label) {
        return label.prefWidth(-1) + 20;
    }
}