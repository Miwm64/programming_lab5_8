package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.animation.AnimationTimer;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import ru.spb.miwm64.moviemanager.client.collectionmanager.ObservableCollection;
import ru.spb.miwm64.moviemanager.client.gui.dialog.UpdateDialog;
import ru.spb.miwm64.moviemanager.client.gui.util.GuiFactory;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;
import ru.spb.miwm64.moviemanager.common.net.Ownership;
import ru.spb.miwm64.moviemanager.common.net.OwnershipType;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MapPane extends VBox {

    private static final double DOT_RADIUS = 5;
    private static final double ZOOM_STEP = 1.1;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 20.0;
    private static final double CORNER_RADIUS = 24.0;

    private double scale = 1.0;

    private final Canvas canvas;
    private final Tooltip tooltip;

    private final CollectionManager collectionManager;
    private final ObservableList<VersionedObject<Movie>> mapEntries;
    private final ListChangeListener<VersionedObject<Movie>> listener;
    private final ObservableList<Ownership> ownerships;

    private double offsetX = 0;
    private double offsetY = 0;

    private double lastMouseX;
    private double lastMouseY;

    private final Map<Object, AnimatedDot> animatedDots = new HashMap<>();
    private AnimationTimer animationTimer;

    private static class ResizableCanvas extends Canvas {
        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double minWidth(double height) {
            return 1.0;
        }

        @Override
        public double minHeight(double width) {
            return 1.0;
        }

        @Override
        public double maxWidth(double height) {
            return Double.MAX_VALUE;
        }

        @Override
        public double maxHeight(double width) {
            return Double.MAX_VALUE;
        }

        @Override
        public void resize(double width, double height) {
            super.setWidth(width);
            super.setHeight(height);
        }
    }

    public MapPane(ObservableCollection collection, CollectionManager collectionManager,
                   ObservableList<Ownership> ownerships) {

        this.ownerships = ownerships;
        this.collectionManager = collectionManager;

        this.setStyle("-fx-background-color: transparent;");
        this.setPadding(new Insets(10));

        this.mapEntries = collection.getRawAll();

        this.listener = c -> {
            syncAnimatedDots();
            redraw();
        };
        this.mapEntries.addListener(listener);

        this.canvas = new ResizableCanvas();

        this.tooltip = new Tooltip();
        tooltip.setAutoHide(true);

        Button zoomIn = new Button();
        zoomIn.textProperty().bind(I18N.createBinding("map_pane.button.zoom_in"));
        Button zoomOut = new Button();
        zoomOut.textProperty().bind(I18N.createBinding("map_pane.button.zoom_out"));

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

        // Margins to shift the buttons away from the top-right corner curve
        StackPane.setMargin(zoomBox, new Insets(12, 16, 0, 0));

        root.setStyle("-fx-background-color: #DAC0A7; -fx-background-radius: " + CORNER_RADIUS + "; -fx-border-radius: " + CORNER_RADIUS + ";");

        Rectangle clip = new Rectangle();
        clip.setArcWidth(CORNER_RADIUS * 2);
        clip.setArcHeight(CORNER_RADIUS * 2);
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);

        VBox.setVgrow(root, Priority.ALWAYS);
        getChildren().add(root);

        canvas.widthProperty().addListener(evt -> redraw());
        canvas.heightProperty().addListener(evt -> redraw());

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

        syncAnimatedDots();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean hasActiveAnimations = animatedDots.values().stream()
                        .anyMatch(d -> d.state != null && d.state != State.IDLE);

                if (hasActiveAnimations) {
                    for (AnimatedDot dot : new ArrayList<>(animatedDots.values())) {
                        dot.update(now);
                        if (dot.state == null) {
                            animatedDots.remove(dot.movie.data);
                        }
                    }
                    redraw();
                }
            }
        };
        animationTimer.start();

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                animationTimer.stop();
            } else {
                animationTimer.start();
            }
        });
    }

    private double worldToScreenX(double x) {
        return (x - offsetX) * scale;
    }

    private double worldToScreenY(double y) {
        return (-y - offsetY) * scale;
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

        for (AnimatedDot dot : animatedDots.values()) {
            dot.draw(gc, this);
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
            boolean hasPermission = ownerships.stream().anyMatch(ow ->
                    ow.userId().equals(JsonRpcRequest.userId) &&
                            ow.movieId().equals(movie.data.getId()) &&
                            (ow.type() == OwnershipType.edit || ow.type() == OwnershipType.owner)
            );
            if (hasPermission) {
                new UpdateDialog(movie.data, collectionManager).showAndWait();
            } else {
                GuiFactory.createErrorPopupWithProperty("map_pane.error.no_permission").showAndWait();
            }
        }
    }

    private VersionedObject<Movie> findMovieAt(double mx, double my) {

        for (AnimatedDot dot : animatedDots.values()) {
            if (dot.state == State.DELETING || dot.state == null) continue;

            double sx = worldToScreenX(dot.currentX);
            double sy = worldToScreenY(dot.currentY);

            double dx = mx - sx;
            double dy = my - sy;

            if (dx * dx + dy * dy <= DOT_RADIUS * DOT_RADIUS) {
                return dot.movie;
            }
        }

        return null;
    }

    /**
     * Generates a color based on the user's UUID using a hex color formula.
     */
    private Color getColorForUser(String userId) {
        if (userId == null) return Color.RED;

        // Formula to get a hex color string from the UUID's hash code
        int hash = userId.hashCode();
        String hexColor = String.format("#%06x", hash & 0xFFFFFF);
        Color color = Color.web(hexColor);

        // Ensure the color is visible (not too dark against the background)
        double brightness = (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
        if (brightness < 0.3) {
            color = color.brighter().brighter();
        }

        return color;
    }

    /**
     * Finds the ownership for a movie and returns the corresponding user color.
     */
    private Color getColorForMovie(VersionedObject<Movie> movie) {
        // Note: Assuming Movie has a getId() method returning Long.
        // If Movie is implemented as a record, you may need to use movie.data.id() instead.
        Long movieId = movie.data.getId();

        for (Ownership o : ownerships) {
            if (o.movieId().equals(movieId)) {
                return getColorForUser(o.userId());
            }
        }

        return Color.RED; // Fallback color if no ownership is found
    }

    private void syncAnimatedDots() {
        Set<Object> currentMovies = new HashSet<>();
        for (VersionedObject<Movie> movie : mapEntries) {
            currentMovies.add(movie.data);
        }

        for (AnimatedDot dot : new ArrayList<>(animatedDots.values())) {
            if (!currentMovies.contains(dot.movie.data)) {
                if (dot.state != State.DELETING && dot.state != null) {
                    dot.state = State.DELETING;
                    dot.startTime = System.nanoTime();
                    dot.duration = 0.8;
                }
            }
        }

        for (VersionedObject<Movie> movie : mapEntries) {
            AnimatedDot dot = animatedDots.get(movie.data);
            if (dot == null) {
                dot = new AnimatedDot(movie, State.CREATING, getColorForMovie(movie));
                animatedDots.put(movie.data, dot);
            } else {
                double newX = movie.data.getCoordinates().getX();
                double newY = movie.data.getCoordinates().getY();
                if (Math.abs(dot.targetX - newX) > 0.001 || Math.abs(dot.targetY - newY) > 0.001) {
                    if (dot.state == State.IDLE) {
                        dot.state = State.UPDATING;
                        dot.startX = dot.targetX;
                        dot.startY = dot.targetY;
                        dot.targetX = newX;
                        dot.targetY = newY;
                        dot.startTime = System.nanoTime();
                        dot.duration = 0.8;
                    } else if (dot.state == State.UPDATING) {
                        dot.startX = dot.currentX;
                        dot.startY = dot.currentY;
                        dot.targetX = newX;
                        dot.targetY = newY;
                        dot.startTime = System.nanoTime();
                    }
                }
            }
        }
    }

    private enum State {
        CREATING, IDLE, UPDATING, DELETING
    }

    private static class AnimatedDot {
        VersionedObject<Movie> movie;
        Color color;
        double startX, startY;
        double targetX, targetY;
        double currentX, currentY;
        double alpha = 1.0;
        double radius = DOT_RADIUS;

        State state = State.CREATING;

        long startTime;
        double duration = 0.6;

        java.util.List<double[]> particles = new ArrayList<>();

        AnimatedDot(VersionedObject<Movie> movie, State initialState, Color color) {
            this.movie = movie;
            this.state = initialState;
            this.color = color;
            this.startTime = System.nanoTime();

            this.targetX = movie.data.getCoordinates().getX();
            this.targetY = movie.data.getCoordinates().getY();

            if (initialState == State.CREATING) {
                this.startX = 0;
                this.startY = 0;
                this.currentX = startX;
                this.currentY = startY;
            } else {
                this.startX = targetX;
                this.startY = targetY;
                this.currentX = targetX;
                this.currentY = targetY;
            }

            if (initialState == State.DELETING) {
                Random rand = new Random();
                for (int i = 0; i < 20; i++) {
                    double angle = rand.nextDouble() * 2 * Math.PI;
                    double speed = 10 + rand.nextDouble() * 20;
                    particles.add(new double[]{
                            Math.cos(angle) * speed,
                            Math.sin(angle) * speed,
                            rand.nextDouble() * 0.5 + 0.5
                    });
                }
            }
        }

        void update(long now) {
            if (state == null) return;

            double elapsed = (now - startTime) / 1_000_000_000.0;
            double progress = Math.min(1.0, elapsed / duration);

            switch (state) {
                case CREATING:
                    currentX = startX + (targetX - startX) * easeOut(progress);
                    currentY = startY + (targetY - startY) * easeOut(progress);
                    alpha = 1.0;
                    if (progress >= 1.0) state = State.IDLE;
                    break;

                case IDLE:
                    currentX = targetX;
                    currentY = targetY;
                    alpha = 1.0;
                    break;

                case UPDATING:
                    currentX = startX + (targetX - startX) * easeInOut(progress);
                    currentY = startY + (targetY - startY) * easeInOut(progress);
                    alpha = 0.5 + 0.5 * Math.sin(progress * Math.PI * 4);
                    if (progress >= 1.0) {
                        state = State.IDLE;
                        alpha = 1.0;
                    }
                    break;

                case DELETING:
                    currentX = targetX;
                    currentY = targetY;
                    alpha = 1.0 - progress;
                    radius = DOT_RADIUS * (1.0 - progress);
                    if (progress >= 1.0) {
                        state = null;
                    }
                    break;
            }
        }

        void draw(GraphicsContext gc, MapPane pane) {
            if (state == null) return;

            double sx = pane.worldToScreenX(currentX);
            double sy = pane.worldToScreenY(currentY);

            gc.save();
            gc.setGlobalAlpha(alpha);
            gc.setFill(color); // Use the dynamically assigned color

            if (state == State.DELETING) {
                gc.fillOval(sx - radius, sy - radius, radius * 2, radius * 2);

                double elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0;
                double progress = Math.min(1.0, elapsed / duration);
                for (double[] p : particles) {
                    double px = sx + p[0] * progress * 10;
                    double py = sy + p[1] * progress * 10;
                    double pAlpha = p[2] * (1.0 - progress);
                    gc.setGlobalAlpha(pAlpha);
                    gc.fillOval(px - 1, py - 1, 2, 2);
                }
            } else {
                gc.fillOval(sx - DOT_RADIUS, sy - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
            }

            gc.restore();
        }

        private double easeOut(double t) {
            return 1 - Math.pow(1 - t, 3);
        }

        private double easeInOut(double t) {
            return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
        }
    }
}