package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.client.Main;
import ru.spb.miwm64.moviemanager.client.collectionmanager.ObservableCollection;
import ru.spb.miwm64.moviemanager.client.gui.dialog.CreateDialog;
import ru.spb.miwm64.moviemanager.client.gui.dialog.MyDialog;
import ru.spb.miwm64.moviemanager.client.gui.dialog.PermissionDialog;
import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import ru.spb.miwm64.moviemanager.client.gui.widgets.TableEntry;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.Ownership;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.Node;
import javafx.geometry.Point2D;

import javafx.util.Duration;

import java.util.HashMap;

// TODO in fullscreen small columns
// TODO help
// TODO command
public class TablePane extends VBox {
    private final ObservableList<VersionedObject<Movie>> tableEntries;
    private final ListChangeListener<VersionedObject<Movie>> tableEntryListChangeListener;
    private final Map<Long, TableEntry> tableEntryMap;

    private final Label titleLabel;
    private final TableEntry columnRow;
    private final VBox entriesVBox;
    private final ScrollPane entriesScrollPane;
    private final Button createButton = new Button();
    private final Button revokeAccessButton = new Button();
    private final Button grantAccessButton = new Button();
    private final ObservableList<Ownership> ownerships;
    private final JsonRpcClient rpcClient;

    private final Logger log = LoggerFactory.getLogger(TablePane.class);
    private final CollectionManager cm;

    private SortColumn currentSortColumn = SortColumn.ID;
    private boolean ascending = true;
    private boolean sorting = false;

    public TablePane(ObservableCollection collectionManager, CollectionManager cm,
                     ObservableList<Ownership> ownerships, JsonRpcClient rpcClient) {
        tableEntryMap = new ConcurrentHashMap<>();
        tableEntries = collectionManager.getRawAll();
        tableEntryListChangeListener = change -> {
            handleUpdate(change);
        };
        tableEntries.addListener(tableEntryListChangeListener);
        this.ownerships = ownerships;
        this.rpcClient = rpcClient;

        createButton.setStyle("-fx-background-color: #EEDEC5;" +
                "-fx-background-radius: 24px;  -fx-border-radius: 24;");
        createButton.setFont(Helper.getBoldFont(18));

        grantAccessButton.setStyle("-fx-background-color: #EEDEC5;" +
                "-fx-background-radius: 24px;  -fx-border-radius: 24;");
        grantAccessButton.setFont(Helper.getBoldFont(18));

        revokeAccessButton.setStyle("-fx-background-color: #EEDEC5;" +
                "-fx-background-radius: 24px;  -fx-border-radius: 24;");
        revokeAccessButton.setFont(Helper.getBoldFont(18));

        this.cm = cm;
        entriesScrollPane = new ScrollPane();
        entriesVBox = new VBox();
        entriesScrollPane.setContent(entriesVBox);
        titleLabel = new Label();
        titleLabel.textProperty().bind(I18N.createBinding("table_pane.label.title"));
        titleLabel.setFont(Helper.getBoldFont(18));
        titleLabel.setStyle("-fx-background-color: #EEDEC5;" +
                "-fx-background-radius: 24px;  -fx-border-radius: 24;");
        columnRow = new TableEntry(false, cm, this::sortBy);
        titleLabel.setPrefWidth(300);
        titleLabel.setAlignment(Pos.CENTER);
        this.setAlignment(Pos.TOP_CENTER);
        VBox.setMargin(titleLabel, new Insets(10, 0, 10, 0));
        VBox.setMargin(columnRow, new Insets(5, 0, 20, 0));
        this.getChildren().add(titleLabel);
        this.getChildren().add(columnRow);
        this.getChildren().add(entriesScrollPane); // TODO selection blue
        entriesScrollPane.setFitToWidth(true);
        entriesScrollPane.hbarPolicyProperty().set(ScrollPane.ScrollBarPolicy.NEVER);
        entriesScrollPane.vbarPolicyProperty().set(ScrollPane.ScrollBarPolicy.NEVER);
        entriesScrollPane.setStyle("-fx-background: #DAC0A7;");
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setStyle("-fx-background-color: #DAC0A7; -fx-background-radius: 20px;");

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().add(revokeAccessButton);
        buttonBox.getChildren().add(createButton);
        buttonBox.getChildren().add(grantAccessButton);

        this.getChildren().add(buttonBox);

        createButton.setOnAction(e -> {
            CreateDialog createPane = new CreateDialog(cm);
            createPane.showAndWait();
        });
        grantAccessButton.setOnAction(e -> {
            MyDialog dialog = new PermissionDialog(true, rpcClient);
            dialog.showAndWait();
        });
        revokeAccessButton.setOnAction(e -> {
            MyDialog dialog = new PermissionDialog(false, rpcClient);
            dialog.showAndWait();
        });

        createButton.textProperty().bind(I18N.createBinding("table_pane.button.create"));
        grantAccessButton.textProperty().bind(I18N.createBinding("table_pane.button.grant_access"));
        revokeAccessButton.textProperty().bind(I18N.createBinding("table_pane.button.revoke_access"));
    }

    private void addEntry(VersionedObject<Movie> movie, CollectionManager cm) {

        Platform.runLater(() -> {

            TableEntry entry =
                    new TableEntry(
                            movie.data,
                            cm,
                            this.ownerships
                    );

            tableEntryMap.put(
                    movie.data.getId(),
                    entry
            );

            rebuildTable();

            animateCreate(entry);
        });
    }

    private void removeEntry(
            VersionedObject<Movie> movie
    ) {

        Platform.runLater(() -> {

            TableEntry entry =
                    tableEntryMap.remove(
                            movie.data.getId()
                    );

            if (entry == null) {
                return;
            }

            animateDelete(
                    entry,
                    () -> entriesVBox
                            .getChildren()
                            .remove(entry)
            );
        });
    }

    private void updateEntry(VersionedObject<Movie> movie) {
        Platform.runLater(() -> {
            TableEntry entry = tableEntryMap.get(movie.data.getId());
            entry.setMovie(movie.data);
        });
    }


    private void handleUpdate(ListChangeListener.Change<? extends VersionedObject<Movie>> change) {
        boolean shouldSort = false;
        if (sorting) {
            return;
        }
        while (change.next()) {

            if (change.wasPermutated()) {
                continue;
            }

            if (change.wasRemoved() && change.wasAdded()) {

                for (VersionedObject<Movie> item : change.getAddedSubList()) {
                    log.info("updated - id: {}", item.data.getId());

                    updateEntry(item);

                    if (item.data.getId() != -1) {
                        shouldSort = true;
                    }
                }

            } else if (change.wasRemoved()) {

                for (VersionedObject<Movie> item : change.getRemoved()) {
                    log.info("deleted - id: {}", item.data.getId());

                    removeEntry(item);
                }

                shouldSort = true;

            } else if (change.wasAdded()) {

                for (VersionedObject<Movie> item : change.getAddedSubList()) {
                    log.info("created - id: {}", item.data.getId());

                    addEntry(item, cm);

                    if (item.data.getId() != -1) {
                        shouldSort = true;
                    }
                }
            }
        }

        if (shouldSort) {
            applyCurrentSorting();
        }
    }

    private void applyCurrentSorting() {

        if (sorting) {
            return;
        }

        sorting = true;

        try {
            FXCollections.sort(
                    tableEntries,
                    createComparator(
                            currentSortColumn,
                            ascending
                    )
            );
        }
        finally {
            sorting = false;
        }
    }

    private Comparator<VersionedObject<Movie>> createComparator( SortColumn column, boolean ascending) {
        Comparator<Movie> movieComparator = switch (column) {

            case ID ->
                    Comparator.comparing(Movie::getId);

            case TITLE ->
                    Comparator.comparing(
                            Movie::getName,
                            String.CASE_INSENSITIVE_ORDER
                    );

            case COORDINATES ->
                    Comparator.comparing(
                            m -> m.getCoordinates().getX()
                    );

            case CREATION_DATE ->
                    Comparator.comparing(
                            Movie::getCreationDate
                    );

            case OSCARS ->
                    Comparator.comparing(
                            Movie::getOscarsCount
                    );

            case GOLDEN_PALM ->
                    Comparator.comparing(
                            Movie::getGoldenPalmCount
                    );

            case GENRE ->
                    Comparator.comparing(
                            m -> m.getGenre().name()
                    );

            case MPAA ->
                    Comparator.comparing(
                            m -> m.getMpaaRating().name()
                    );
        };

        Comparator<VersionedObject<Movie>> comparator =
                Comparator.comparing(
                        vo -> vo.data,
                        movieComparator.thenComparing(Movie::getId)
                );

        return ascending
                ? comparator
                : comparator.reversed();
    }

    private void rebuildTable() {

        entriesVBox
                .getChildren()
                .clear();

        for (VersionedObject<Movie> movie :
                tableEntries) {

            TableEntry entry =
                    tableEntryMap.get(
                            movie.data.getId()
                    );

            if (entry == null) {
                continue;
            }

            VBox.setMargin(
                    entry,
                    new Insets(0,0,5,0)
            );

            entriesVBox
                    .getChildren()
                    .add(entry);
        }
    }
    private void sortBy(SortColumn column) {

        if (currentSortColumn == column) {
            ascending = !ascending;
        } else {
            currentSortColumn = column;
            ascending = true;
        }

        animateSort();
    }

    private void animateSort() {
        Map<Node, Double> oldY = new HashMap<>();

        for (Node node : entriesVBox.getChildren()) {
            oldY.put(
                    node,
                    node.getBoundsInParent().getMinY()
            );
        }

        sorting = true;

        try {

            FXCollections.sort(
                    tableEntries,
                    createComparator(
                            currentSortColumn,
                            ascending
                    )
            );

            rebuildTable();

            entriesVBox.applyCss();
            entriesVBox.layout();

            Platform.runLater(() -> {

                try {

                    for (Node node : entriesVBox.getChildren()) {

                        double newY =
                                node.getBoundsInParent()
                                        .getMinY();

                        double previousY =
                                oldY.getOrDefault(
                                        node,
                                        newY
                                );

                        double delta =
                                previousY - newY;


                        if (Math.abs(delta) < 1) {
                            continue;
                        }

                        node.setTranslateY(0);
                        node.setTranslateY(delta);

                        TranslateTransition tt =
                                new TranslateTransition(
                                        Duration.millis(500),
                                        node
                                );

                        tt.setInterpolator(
                                Interpolator.EASE_BOTH
                        );

                        tt.setToY(0);

                        tt.play();
                    }

                } finally {
                    sorting = false;
                }
            });

        } catch (Exception e) {

            sorting = false;
            throw e;
        }
    }

    private void animateCreate(TableEntry entry) {

        entry.setOpacity(0);
        entry.setScaleY(0.8);

        FadeTransition fade =
                new FadeTransition(
                        Duration.millis(250),
                        entry
                );

        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale =
                new ScaleTransition(
                        Duration.millis(250),
                        entry
                );

        scale.setFromY(0.8);
        scale.setToY(1);

        new ParallelTransition(
                fade,
                scale
        ).play();
    }

    private void animateDelete(
            TableEntry entry,
            Runnable after
    ) {

        FadeTransition fade =
                new FadeTransition(
                        Duration.millis(250),
                        entry
                );

        fade.setFromValue(1);
        fade.setToValue(0);

        ScaleTransition scale =
                new ScaleTransition(
                        Duration.millis(250),
                        entry
                );

        scale.setFromY(1);
        scale.setToY(0.8);

        ParallelTransition pt =
                new ParallelTransition(
                        fade,
                        scale
                );

        pt.setOnFinished(
                e -> after.run()
        );

        pt.play();
    }
}
