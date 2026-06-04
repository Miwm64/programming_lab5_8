package ru.spb.miwm64.moviemanager.client.gui.pane;

import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
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

public class TablePane extends VBox {
//    private final ObservableList<String> tableEntries;
//    private final ListChangeListener<String> tableEntryListChangeListener;
    private final Label titleLabel;
    private final TableEntry columnRow;
    private final VBox entriesVBox;
    private final ScrollPane entriesScrollPane;
    private final Button createButton = new Button("Create");
    public TablePane() {
        entriesScrollPane = new ScrollPane();
        entriesVBox = new VBox();
        entriesScrollPane.setContent(entriesVBox);
        titleLabel = new Label("Movie table");
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
        for (int i = 0; i < 10; ++i){
            addEntry();
        }
    }

    private void addEntry() {
        TableEntry entry = new TableEntry(true);
        entriesVBox.getChildren().add(entry);
        VBox.setMargin(entry,  new Insets(0, 0, 5, 0));
    }
}
