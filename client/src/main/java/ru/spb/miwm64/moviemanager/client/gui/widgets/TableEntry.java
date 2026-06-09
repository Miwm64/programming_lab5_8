package ru.spb.miwm64.moviemanager.client.gui.widgets;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.spb.miwm64.moviemanager.client.command.Command;
import ru.spb.miwm64.moviemanager.client.command.Parameter;
import ru.spb.miwm64.moviemanager.client.commands.RemoveByIDCommand;
import ru.spb.miwm64.moviemanager.client.gui.dialog.ConfirmationDialog;
import ru.spb.miwm64.moviemanager.client.gui.dialog.CreateDialog;
import ru.spb.miwm64.moviemanager.client.gui.dialog.MyDialog;
import ru.spb.miwm64.moviemanager.client.gui.dialog.UpdateDialog;
import ru.spb.miwm64.moviemanager.client.gui.pane.SortColumn;
import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;
import ru.spb.miwm64.moviemanager.common.net.Ownership;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

// TODO i18n
public class TableEntry extends HBox {
    private Button editButton = new Button();
    private Label title = createLabel("Title");
    private Label id = createLabel("ID");
    private Label coordinate = createLabel("Coordinates");
    private Label creationDate = createLabel("Creation date");
    private Label oscars = createLabel("Oscars");
    private Label goldenPalm = createLabel("Golden Palm");
    private Label genre = createLabel("Genre");
    private Label MPAA_RATING = createLabel("MPAA");
    private Label person =  createLabel("Person");
    private Button deleteButton = new Button();
    private Movie movie;
    private CollectionManager collectionManager;
    private Consumer<SortColumn> sortHandler;
    private ObservableList<Ownership> ownerships;

    private static final DoubleProperty titleWidth = new SimpleDoubleProperty(100);
    private static final DoubleProperty idWidth = new SimpleDoubleProperty(30);
    private static final DoubleProperty coordWidth = new SimpleDoubleProperty(100);
    private static final DoubleProperty dateWidth = new SimpleDoubleProperty(100);
    private static final DoubleProperty oscarsWidth = new SimpleDoubleProperty(60);
    private static final DoubleProperty palmWidth = new SimpleDoubleProperty(80);
    private static final DoubleProperty genreWidth = new SimpleDoubleProperty(80);
    private static final DoubleProperty ratingWidth = new SimpleDoubleProperty(50);
    private static final DoubleProperty personWidth = new SimpleDoubleProperty(100);

    public TableEntry(boolean showButtons, CollectionManager collectionManager,
                      Consumer<SortColumn> sortHandler) {
        this();
        this.sortHandler = sortHandler;

        if (!showButtons) {
            editButton.setVisible(false);
            deleteButton.setVisible(false);

            makeHeaderClickable();
        }

        this.collectionManager = collectionManager;
    }
    public TableEntry(Movie movie, CollectionManager collectionManager, ObservableList<Ownership> ownerships) {
        this();
        this.ownerships = ownerships;
        setMovie(movie);
        this.collectionManager = collectionManager;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        this.id.setText(""+movie.getId());
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
        editButton.setMinWidth(Button.USE_PREF_SIZE);
        deleteButton.setMinWidth(Button.USE_PREF_SIZE);
        editButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setMaxWidth(Double.MAX_VALUE);

        this.getChildren().addAll(editButton, id, title, coordinate, creationDate,
                oscars, goldenPalm, genre, MPAA_RATING, person, deleteButton);

        // 3. Bind label widths (unchanged)
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
        HBox.setHgrow(editButton, Priority.NEVER);
        HBox.setHgrow(deleteButton, Priority.NEVER);

        id.setMinWidth(30);
        id.setMaxWidth(50);
        HBox.setMargin(editButton, new Insets(0, 10, 0, 0));
        HBox.setMargin(deleteButton, new Insets(0, 0, 0, 10));
        setMinWidth(800);

        String resourcePath = "/images/edit.png";
        var resourceUrl = getClass().getResourceAsStream(resourcePath);
        Image icon = new Image(resourceUrl);
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        imageView.setPreserveRatio(true);
        editButton.setStyle("-fx-background-color: transparent;");
        editButton.setGraphic(imageView);


        String resourcePath2 = "/images/delete.png";
        var resourceUrl2 = getClass().getResourceAsStream(resourcePath2);
        Image icon2 = new Image(resourceUrl2);
        ImageView imageView2 = new ImageView(icon2);
        imageView2.setFitWidth(24);
        imageView2.setFitHeight(24);
        imageView2.setPreserveRatio(true);
        deleteButton.setStyle("-fx-background-color: transparent;");
        deleteButton.setGraphic(imageView2);

        editButton.setOnMouseClicked(event -> {
//            if (this.ownerships.contains(new Ownership(JsonRpcRequest.)))
            MyDialog dialog = new UpdateDialog(movie, collectionManager);
            dialog.showAndWait();
        });
        deleteButton.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                ConfirmationDialog dialog = new ConfirmationDialog("Do you really want to delete this movie?",
                        "Movie deletion");
                dialog.showAndWait().ifPresent(result -> {
                    if (!result) {
                        return;
                    }

                    Command deleteCommand = new RemoveByIDCommand(collectionManager);
                    Parameter param = deleteCommand.getParams().get(0);
                    param.fromString(movie.getId()+"");
                    deleteCommand.setParam(param);
                    deleteCommand.execute();
                });
            });
        });
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

    private void makeHeaderClickable() {

        id.setOnMouseClicked(e ->
                sortHandler.accept(SortColumn.ID));

        title.setOnMouseClicked(e ->
                sortHandler.accept(SortColumn.TITLE));

        coordinate.setOnMouseClicked(e ->
                sortHandler.accept(SortColumn.COORDINATES));

        creationDate.setOnMouseClicked(e ->
                sortHandler.accept(SortColumn.CREATION_DATE));

        oscars.setOnMouseClicked(e ->
                sortHandler.accept(SortColumn.OSCARS));

        goldenPalm.setOnMouseClicked(e ->
                sortHandler.accept(SortColumn.GOLDEN_PALM));

        genre.setOnMouseClicked(e ->
                sortHandler.accept(SortColumn.GENRE));

        MPAA_RATING.setOnMouseClicked(e ->
                sortHandler.accept(SortColumn.MPAA));
    }
}