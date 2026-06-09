package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import ru.spb.miwm64.moviemanager.client.collectionmanager.ObservableCollection;
import ru.spb.miwm64.moviemanager.client.gui.dialog.MyDialog;
import ru.spb.miwm64.moviemanager.client.gui.dialog.UpdateDialog;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

public class MapPane extends VBox {
    private static final double DOT_RADIUS = 5;

    private final Canvas canvas;
    private final Tooltip tooltip;

    private final CollectionManager collectionManager;
    private final ObservableList<VersionedObject<Movie>> mapEntries;
    private final ListChangeListener<VersionedObject<Movie>> mapEntryListChangeListener;

    private double offsetX;
    private double offsetY;

    private double lastMouseX;
    private double lastMouseY;

    public MapPane(ObservableCollection collection, CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.setStyle("-fx-background-color: white;");
        mapEntries = collection.getRawAll();
        mapEntryListChangeListener = change -> {
            redraw();
        };
        mapEntries.addListener(mapEntryListChangeListener);


        canvas = new Canvas();
        canvas.setStyle("-fx-background-color: blue;");

        getChildren().add(canvas);

        widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            redraw();
        });

        heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            redraw();
        });
        canvas.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        canvas.setOnMouseDragged(event -> {

            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;

            offsetX -= dx;
            offsetY -= dy;

            lastMouseX = event.getX();
            lastMouseY = event.getY();

            redraw();
        });

        canvas.setOnMouseMoved(event -> handleHover(
                event
        ));

        canvas.setOnMouseClicked(event -> handleClick(
                event.getX(),
                event.getY()
        ));

        tooltip = new Tooltip();
        tooltip.setAutoHide(true);
        tooltip.setAutoFix(true);

        redraw();
    }

    private void redraw() {

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight()
        );

        double originScreenX = -offsetX;
        double originScreenY = -offsetY;

        gc.strokeLine(
                0,
                originScreenY,
                canvas.getWidth(),
                originScreenY
        );

        gc.strokeLine(
                originScreenX,
                0,
                originScreenX,
                canvas.getHeight()
        );

        gc.strokeLine(
                canvas.getWidth() - 10,
                originScreenY - 5,
                canvas.getWidth(),
                originScreenY
        );

        gc.strokeLine(
                canvas.getWidth() - 10,
                originScreenY + 5,
                canvas.getWidth(),
                originScreenY
        );

        gc.strokeLine(
                originScreenX - 5,
                10,
                originScreenX,
                0
        );

        gc.strokeLine(
                originScreenX + 5,
                10,
                originScreenX,
                0
        );


        for (VersionedObject<Movie> movie : mapEntries) {

            double worldX =
                    movie.data.getCoordinates().getX();

            double worldY =
                    movie.data.getCoordinates().getY();

            double screenX = worldX - offsetX;
            double screenY = worldY - offsetY;

            gc.fillOval(
                    screenX - 5,
                    screenY - 5,
                    10,
                    10
            );
        }
    }

    private void handleHover(MouseEvent event) {

        VersionedObject<Movie> movie =
                findMovieAt(event.getX(), event.getY());

        if (movie != null) {

            tooltip.setText(movie.data.toString());

            double screenX = event.getScreenX() + 10;
            double screenY = event.getScreenY() + 10;

            if (!tooltip.isShowing()) {
                tooltip.show(canvas, screenX, screenY);
            } else {
                tooltip.setAnchorX(screenX);
                tooltip.setAnchorY(screenY);
            }

        } else {
            tooltip.hide();
        }
    }

    private void handleClick(double mouseX, double mouseY) {
        VersionedObject<Movie> movie =
                findMovieAt(mouseX, mouseY);

        if (movie != null) {
            MyDialog dialog = new UpdateDialog(movie.data, collectionManager);
            dialog.showAndWait();
        }
    }

    private VersionedObject<Movie> findMovieAt(
            double mouseX,
            double mouseY
    ) {

        for (VersionedObject<Movie> movie : mapEntries) {

            double screenX =
                    movie.data.getCoordinates().getX() - offsetX;

            double screenY =
                    movie.data.getCoordinates().getY() - offsetY;

            double dx = mouseX - screenX;
            double dy = mouseY - screenY;

            if (dx * dx + dy * dy <= DOT_RADIUS * DOT_RADIUS) {
                return movie;
            }
        }

        return null;
    }
}