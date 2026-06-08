package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.client.Main;
import ru.spb.miwm64.moviemanager.client.collectionmanager.ObservableCollection;
import ru.spb.miwm64.moviemanager.client.gui.dialog.CreateDialog;
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
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
// todo animations
public class TablePane extends VBox {
    private final ObservableList<VersionedObject<Movie>> tableEntries;
    private final ListChangeListener<VersionedObject<Movie>> tableEntryListChangeListener;
    private final Map<Long, TableEntry> tableEntryMap;

    private final Label titleLabel;
    private final TableEntry columnRow;
    private final VBox entriesVBox;
    private final ScrollPane entriesScrollPane;
    private final Button createButton = new Button();

    private final Logger log = LoggerFactory.getLogger(TablePane.class);
    private final CollectionManager cm;

    private SortColumn currentSortColumn = SortColumn.ID;
    private boolean ascending = true;

    public TablePane(ObservableCollection collectionManager, CollectionManager cm) {
        tableEntryMap = new ConcurrentHashMap<>();
        tableEntries = collectionManager.getRawAll();
        tableEntryListChangeListener = change -> {
            handleUpdate(change);
        };
        tableEntries.addListener(tableEntryListChangeListener);


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
        this.getChildren().add(createButton);
        createButton.setOnAction(e -> {
            CreateDialog createPane = new CreateDialog(cm);
            createPane.showAndWait();
        });

        createButton.textProperty().bind(I18N.createBinding("table_pane.button.create"));
    }

    private void addEntry(VersionedObject<Movie> movie, CollectionManager cm) {
        Platform.runLater(() -> {
            TableEntry entry = new TableEntry(movie.data, cm);
            entriesVBox.getChildren().add(entry);
            VBox.setMargin(entry, new Insets(0, 0, 5, 0));
            tableEntryMap.put(movie.data.getId(), entry);
        });
    }

    private void removeEntry(VersionedObject<Movie> movie) {
        Platform.runLater(() -> {
            TableEntry entry = tableEntryMap.remove(movie.data.getId());
            entriesVBox.getChildren().remove(entry);
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

        while (change.next()) {

            if (change.wasPermutated()) {
                refreshTable();
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
        FXCollections.sort(
                tableEntries,
                createComparator(currentSortColumn, ascending)
        );
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

    private void refreshTable() {

        Platform.runLater(() -> {

            entriesVBox.getChildren().clear();

            for (VersionedObject<Movie> movie : tableEntries) {

                TableEntry entry =
                        tableEntryMap.get(movie.data.getId());

                if (entry != null) {

                    VBox.setMargin(
                            entry,
                            new Insets(0, 0, 5, 0)
                    );

                    entriesVBox.getChildren().add(entry);
                }
            }
        });
    }

    private void sortBy(SortColumn column) {

        if (currentSortColumn == column) {
            ascending = !ascending;
        } else {
            currentSortColumn = column;
            ascending = true;
        }

        FXCollections.sort(
                tableEntries,
                createComparator(currentSortColumn, ascending)
        );
    }
}
