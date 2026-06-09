package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import ru.spb.miwm64.moviemanager.client.collectionmanager.ObservableCollection;
import ru.spb.miwm64.moviemanager.client.gui.dialog.CreateDialog;
import ru.spb.miwm64.moviemanager.client.gui.dialog.UpdateDialog;
import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

public class MapPane extends BorderPane {

    private static final double MAP_WIDTH = 2000;
    private static final double MAP_HEIGHT = 2000;

    private final ObservableCollection collection;
    private final CollectionManager cm;

    private double zoom = 1.0;

    private final Pane mapArea = new Pane();
    private final Group zoomGroup = new Group(mapArea);

    private final ScrollPane scrollPane = new ScrollPane();

    private final Button createButton = new Button();
    private final Button zoomInButton = new Button("+");
    private final Button zoomOutButton = new Button("-");

    private final Label zoomLabel = new Label("100%");
    private final Label titleLabel = new Label();

    public MapPane(
            ObservableCollection collection,
            CollectionManager cm
    ) {
        this.collection = collection;
        this.cm = cm;

        setPadding(new Insets(10));
        setStyle("-fx-background-color: #DAC0A7;");

        // --- title label ---
        titleLabel.textProperty().bind(
                I18N.createBinding("map_pane.label.title")
        );
        titleLabel.setFont(Helper.getBoldFont(18));
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24px;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 10, 0));
        setTop(titleLabel);

        // --- map area and scroll pane ---
        mapArea.setPrefSize(MAP_WIDTH, MAP_HEIGHT);
        mapArea.setMinSize(MAP_WIDTH, MAP_HEIGHT);
        scrollPane.setContent(zoomGroup);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(false);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setCenter(scrollPane);

        // --- bottom controls (create & zoom) ---
        createButton.textProperty().bind(I18N.createBinding("table_pane.button.create"));
        createButton.setFont(Helper.getBoldFont(18));
        createButton.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24px;");
        createButton.setOnAction(e -> {
            CreateDialog dialog = new CreateDialog(cm);
            dialog.showAndWait();
        });

        zoomInButton.setFont(Helper.getBoldFont(18));
        zoomOutButton.setFont(Helper.getBoldFont(18));
        zoomInButton.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24px;");
        zoomOutButton.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24px;");
        zoomLabel.setFont(Helper.getBoldFont(16));

        zoomInButton.setOnAction(e -> {
            zoom *= 1.25;
            zoomGroup.setScaleX(zoom);
            zoomGroup.setScaleY(zoom);
            updateZoomLabel();
        });

        zoomOutButton.setOnAction(e -> {
            zoom /= 1.25;
            zoomGroup.setScaleX(zoom);
            zoomGroup.setScaleY(zoom);
            updateZoomLabel();
        });

        HBox zoomBar = new HBox(20, zoomOutButton, zoomLabel, zoomInButton);
        zoomBar.setAlignment(Pos.CENTER);
        zoomBar.setPadding(new Insets(10));

        HBox createBar = new HBox(createButton);
        createBar.setAlignment(Pos.CENTER);
        createBar.setPadding(new Insets(10));

        VBox bottom = new VBox(zoomBar, createBar);
        bottom.setAlignment(Pos.CENTER);
        setBottom(bottom);

        // --- listen for collection changes and redraw ---
        collection.getRawAll().addListener(
                (ListChangeListener<VersionedObject<Movie>>) change -> redraw()
        );

        // Perform the initial redraw (grid, axes, movies)
        redraw();

        // ---- ONLY CHANGE: force centering even if viewport is 0 ----
        forceCenterOnOrigin();
    }

    // New method: retry centering until it works (ignores zero viewport)
    private void forceCenterOnOrigin() {
        if (centerOnOrigin()) return;
        Platform.runLater(() -> {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
            pause.setOnFinished(e -> forceCenterOnOrigin());
            pause.play();
        });
    }

    // Fixed centering logic (works correctly with zoom scaling)
    private boolean centerOnOrigin() {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        if (viewportWidth <= 0 || viewportHeight <= 0) return false;

        // Origin in unscaled coordinates (center of the map)
        double originX = MAP_WIDTH / 2.0;
        double originY = MAP_HEIGHT / 2.0;

        // Scroll offset so origin is in the center, accounting for zoom
        double scrollX = originX - (viewportWidth / 2.0) / zoom;
        double scrollY = originY - (viewportHeight / 2.0) / zoom;

        double maxScrollX = MAP_WIDTH - viewportWidth;
        double maxScrollY = MAP_HEIGHT - viewportHeight;
        scrollX = Math.min(Math.max(scrollX, 0), maxScrollX);
        scrollY = Math.min(Math.max(scrollY, 0), maxScrollY);

        double hvalue = (maxScrollX > 0) ? scrollX / maxScrollX : 0.5;
        double vvalue = (maxScrollY > 0) ? scrollY / maxScrollY : 0.5;

        scrollPane.setHvalue(hvalue);
        scrollPane.setVvalue(vvalue);
        return true;
    }

    private void redraw() {
        Platform.runLater(() -> {
            mapArea.getChildren().clear();
            drawGrid();
            drawAxes();
            for (VersionedObject<Movie> vo : collection.getRawAll()) {
                addMovieNode(vo.data);
            }
            // Optional: try centering once after redraw (already forced by constructor)
            centerOnOrigin();
        });
    }

    // --- drawing helpers (unchanged) ---
    private void drawGrid() {
        for (int i = 0; i <= 20; i++) {
            double x = (MAP_WIDTH / 20.0) * i;
            Line line = new Line(x, 0, x, MAP_HEIGHT);
            line.setStroke(Color.rgb(0, 0, 0, 0.1));
            mapArea.getChildren().add(line);
        }
        for (int i = 0; i <= 20; i++) {
            double y = (MAP_HEIGHT / 20.0) * i;
            Line line = new Line(0, y, MAP_WIDTH, y);
            line.setStroke(Color.rgb(0, 0, 0, 0.1));
            mapArea.getChildren().add(line);
        }
    }

    private void drawAxes() {
        double centerX = MAP_WIDTH / 2.0;
        double centerY = MAP_HEIGHT / 2.0;

        Line xAxis = new Line(0, centerY, MAP_WIDTH, centerY);
        xAxis.setStrokeWidth(2);
        Line yAxis = new Line(centerX, 0, centerX, MAP_HEIGHT);
        yAxis.setStrokeWidth(2);
        mapArea.getChildren().addAll(xAxis, yAxis);

        Label xLabel = new Label("X");
        xLabel.setStyle("-fx-font-weight: bold;");
        xLabel.setLayoutX(MAP_WIDTH - 30);
        xLabel.setLayoutY(centerY + 5);

        Label yLabel = new Label("Y");
        yLabel.setStyle("-fx-font-weight: bold;");
        yLabel.setLayoutX(centerX + 5);
        yLabel.setLayoutY(10);

        mapArea.getChildren().addAll(xLabel, yLabel);
    }

    private void addMovieNode(Movie movie) {
        double x = convertX(movie.getCoordinates().getX());
        double y = convertY(movie.getCoordinates().getY());

        Circle circle = new Circle(3);
        circle.setFill(Color.RED);
        Label label = new Label(movie.getName());
        HBox marker = new HBox(6, circle, label);
        marker.setAlignment(Pos.CENTER_LEFT);
        marker.setLayoutX(x);
        marker.setLayoutY(y);

        Tooltip.install(marker, new Tooltip(
                """
                %s
                
                Id: %d
                Oscars: %d
                Golden Palm: %d
                Coordinates: (%.2f, %d)
                """
                        .formatted(
                                movie.getName(),
                                movie.getId(),
                                movie.getOscarsCount(),
                                movie.getGoldenPalmCount(),
                                movie.getCoordinates().getX(),
                                movie.getCoordinates().getY()
                        )
        ));

        marker.setOnMouseClicked(e -> {
            UpdateDialog dialog = new UpdateDialog(movie, cm);
            dialog.showAndWait();
        });

        mapArea.getChildren().add(marker);
    }

    private double convertX(double x) {
        return MAP_WIDTH / 2.0 + x;
    }

    private double convertY(double y) {
        return MAP_HEIGHT / 2.0 + y;
    }

    private void updateZoomLabel() {
        zoomLabel.setText(String.format("%.0f%%", zoom * 100));
    }
}