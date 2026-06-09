package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import ru.spb.miwm64.moviemanager.client.collectionmanager.ObservableCollection;
import ru.spb.miwm64.moviemanager.client.gui.dialog.UpdateDialog;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

public class MapPane extends VBox {

    private static final double DOT_RADIUS = 5;
    private static final double ZOOM_STEP = 1.1;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 20.0;

    private double scale = 1.0;

    private final Canvas canvas;
    private final Tooltip tooltip;

    private final CollectionManager collectionManager;
    private final ObservableList<VersionedObject<Movie>> mapEntries;
    private final ListChangeListener<VersionedObject<Movie>> listener;

    private double offsetX = 0;
    private double offsetY = 0;

    private double lastMouseX;
    private double lastMouseY;

    public MapPane(ObservableCollection collection, CollectionManager collectionManager) {

        this.collectionManager = collectionManager;
        this.setStyle("-fx-background-color: white;");

        this.mapEntries = collection.getRawAll();

        this.listener = c -> redraw();
        this.mapEntries.addListener(listener);

        this.canvas = new Canvas();

        this.tooltip = new Tooltip();
        tooltip.setAutoHide(true);

        Button zoomIn = new Button("+");
        Button zoomOut = new Button("-");

        zoomIn.setFocusTraversable(false);
        zoomOut.setFocusTraversable(false);

        zoomIn.setOnAction(e -> {
            scale = Math.min(MAX_SCALE, scale * ZOOM_STEP);
            redraw();
        });

        zoomOut.setOnAction(e -> {
            scale = Math.max(MIN_SCALE, scale / ZOOM_STEP);
            redraw();
        });

        HBox zoomBox = new HBox(6, zoomIn, zoomOut);
        zoomBox.setPickOnBounds(false);

        StackPane root = new StackPane(canvas, zoomBox);
        StackPane.setAlignment(zoomBox, Pos.TOP_RIGHT);

        getChildren().add(root);

        widthProperty().addListener((obs, o, n) -> {
            canvas.setWidth(n.doubleValue());
            redraw();
        });

        heightProperty().addListener((obs, o, n) -> {
            canvas.setHeight(n.doubleValue());
            redraw();
        });

        canvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        canvas.setOnMouseDragged(e -> {

            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;

            offsetX -= dx / scale;
            offsetY -= dy / scale;

            lastMouseX = e.getX();
            lastMouseY = e.getY();

            redraw();
        });

        canvas.setOnScroll(e -> {

            double oldScale = scale;

            if (e.getDeltaY() > 0) scale *= ZOOM_STEP;
            else scale /= ZOOM_STEP;

            scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));

            double mx = e.getX();
            double my = e.getY();

            offsetX = mx / oldScale + offsetX - mx / scale;
            offsetY = my / oldScale + offsetY - my / scale;

            redraw();
        });

        canvas.setOnMouseMoved(this::handleHover);
        canvas.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));

        redraw();
    }

    private double worldToScreenX(double x) {
        return (x - offsetX) * scale;
    }

    private double worldToScreenY(double y) {
        return (y - offsetY) * scale;
    }

    private void redraw() {

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);

        double originX = worldToScreenX(0);
        double originY = worldToScreenY(0);

        gc.strokeLine(0, originY, canvas.getWidth(), originY);
        gc.strokeLine(originX, 0, originX, canvas.getHeight());

        gc.setFill(Color.RED);

        for (VersionedObject<Movie> movie : mapEntries) {

            double x = worldToScreenX(movie.data.getCoordinates().getX());
            double y = worldToScreenY(movie.data.getCoordinates().getY());

            gc.fillOval(
                    x - DOT_RADIUS,
                    y - DOT_RADIUS,
                    DOT_RADIUS * 2,
                    DOT_RADIUS * 2
            );
        }
    }

    private void handleHover(MouseEvent e) {

        VersionedObject<Movie> movie = findMovieAt(e.getX(), e.getY());

        if (movie == null) {
            tooltip.hide();
            return;
        }

        tooltip.setText(movie.data.toString());

        double sx = e.getScreenX() + 10;
        double sy = e.getScreenY() + 10;

        if (!tooltip.isShowing()) {
            tooltip.show(canvas, sx, sy);
        } else {
            tooltip.setAnchorX(sx);
            tooltip.setAnchorY(sy);
        }
    }

    private void handleClick(double x, double y) {

        VersionedObject<Movie> movie = findMovieAt(x, y);

        if (movie != null) {
            new UpdateDialog(movie.data, collectionManager).showAndWait();
        }
    }

    private VersionedObject<Movie> findMovieAt(double mx, double my) {

        for (VersionedObject<Movie> m : mapEntries) {

            double sx = worldToScreenX(m.data.getCoordinates().getX());
            double sy = worldToScreenY(m.data.getCoordinates().getY());

            double dx = mx - sx;
            double dy = my - sy;

            if (dx * dx + dy * dy <= DOT_RADIUS * DOT_RADIUS) {
                return m;
            }
        }

        return null;
    }
}