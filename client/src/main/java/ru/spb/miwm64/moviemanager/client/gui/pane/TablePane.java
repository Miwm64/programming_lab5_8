package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.client.Main;
import ru.spb.miwm64.moviemanager.client.collectionmanager.ObservableCollection;
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

public class TablePane extends VBox {
    private final ObservableList<VersionedObject<Movie>> tableEntries;
    private final ListChangeListener<VersionedObject<Movie>> tableEntryListChangeListener;

    private final Label titleLabel;
    private final TableEntry columnRow;
    private final VBox entriesVBox;
    private final ScrollPane entriesScrollPane;
    private final Button createButton = new Button();

    private final Logger log = LoggerFactory.getLogger(TablePane.class);


    public TablePane(ObservableCollection collectionManager) {
        tableEntries = collectionManager.getRawAll();
        tableEntryListChangeListener = change -> {
            handleUpdate(change);
        };
        tableEntries.addListener(tableEntryListChangeListener);


        entriesScrollPane = new ScrollPane();
        entriesVBox = new VBox();
        entriesScrollPane.setContent(entriesVBox);
        titleLabel = new Label();
        titleLabel.textProperty().bind(I18N.createBinding("table_pane.label.title"));
        titleLabel.setFont(Helper.getBoldFont(18));
        titleLabel.setStyle("-fx-background-color: #EEDEC5;" +
                "-fx-background-radius: 24px;  -fx-border-radius: 24;");
        columnRow = new TableEntry(false);
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
            CreatePane createPane = new CreatePane();
            createPane.show();
        });

        createButton.textProperty().bind(I18N.createBinding("table_pane.button.create"));
    }

    private void addEntry(VersionedObject<Movie> movie) {
        Platform.runLater(() -> {
            TableEntry entry = new TableEntry(movie.data);
            entriesVBox.getChildren().add(entry);
            VBox.setMargin(entry, new Insets(0, 0, 5, 0));
        });
    }

    private void removeEntry(VersionedObject<Movie> movie) {
        Platform.runLater(() -> {
            TableEntry entry = new TableEntry(movie.data);
            entriesVBox.getChildren().add(entry);
            VBox.setMargin(entry, new Insets(0, 0, 5, 0));
        });
    }

    private void updateEntry(VersionedObject<Movie> movie) {
        Platform.runLater(() -> {
        TableEntry entry = new TableEntry(movie.data);
        entriesVBox.getChildren().add(entry);
        VBox.setMargin(entry,  new Insets(0, 0, 5, 0));
        });
    }

    private void handleUpdate(ListChangeListener.Change<? extends VersionedObject<Movie>> change){
        while (change.next()) {
            if (change.wasRemoved() && change.wasAdded()) {
                for (VersionedObject<Movie> item : change.getAddedSubList()) {
                    log.info("updated - id: " + item.data.getId());
                    updateEntry(item);
                }
            }
            else if (change.wasRemoved()) {
                for (VersionedObject<Movie> item : change.getRemoved()) {
                    log.info("deleted - id: " + item.data.getId());
                    removeEntry(item);
                }
            }
            else if (change.wasAdded()) {
                for (VersionedObject<Movie> item : change.getAddedSubList()) {
                    log.info("created - id: " + item.data.getId());
                    addEntry(item);
                }
            }
        }
    }
}
