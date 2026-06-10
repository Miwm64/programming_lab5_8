package ru.spb.miwm64.moviemanager.client.gui.widgets;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;

import java.util.Locale;

public class LanguageSelector extends ComboBox<String> {
    private final ObservableList<String> langs;

    public LanguageSelector() {
        this.setMaxWidth(30);
        langs = FXCollections.observableArrayList();
        langs.addAll("English", "Français", "Svenska", "Español (Spain)");
        this.setItems(langs);

        // Assign a unique style class to avoid affecting other standard ComboBoxes
        this.getStyleClass().add("language-selector-combo");

        // Inject CSS to remove the borders, background, focus glow, and the arrow symbol
        String css = "data:text/css," +
                ".language-selector-combo {" +
                "    -fx-background-color: transparent;" +
                "    -fx-background-insets: 0;" +
                "    -fx-background-radius: 0;" +
                "    -fx-padding: 0;" +
                "    -fx-cursor: hand;" + // Shows a hand cursor when hovering over the image
                "}" +
                ".language-selector-combo:focused {" +
                "    -fx-background-color: transparent;" +
                "}" +
                ".language-selector-combo .arrow-button {" +
                "    -fx-padding: 0;" +
                "    -fx-background-color: transparent;" +
                "    -fx-max-width: 0;" +
                "    -fx-min-width: 0;" +
                "    -fx-pref-width: 0;" +
                "}" +
                ".language-selector-combo .arrow {" +
                "    -fx-background-color: transparent;" +
                "    -fx-padding: 0;" +
                "    -fx-shape: null;" +
                "}";
        this.getStylesheets().add(css);

        // Define how items are rendered inside the dropdown list
        this.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    updateGraphic(isSelected());
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (getItem() != null) {
                    updateGraphic(selected);
                }
            }

            private void updateGraphic(boolean selected) {
                if (selected) {
                    setGraphic(new Label("✓"));
                } else {
                    setGraphic(null);
                }
            }
        });

        // Define how the selected item is rendered on the ComboBox face itself
        this.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(""); // Keep text completely empty
                    setGraphic(createIcon()); // Only show the PNG picture
                }
            }
        });

        this.getSelectionModel().select(0);

        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            switch (newValue) {
                case "English": {
                    I18N.setLocale(Locale.ENGLISH);
                    break;
                }
                case "Français": {
                    I18N.setLocale(Locale.FRENCH);
                    break;
                }
                case "Svenska": {
                    I18N.setLocale(new Locale("sv"));
                    break;
                }
                case "Español (Spain)": {
                    I18N.setLocale(new Locale("es", "ES"));
                    break;
                }
                case "Русский": {
                    I18N.setLocale(new Locale("ru"));
                    break;
                }
                default: {
                    I18N.setLocale(Locale.ENGLISH);
                }
            }
        });
    }

    /**
     * Loads the PNG image resource and configures an ImageView to display it.
     */
    private Node createIcon() {
        String resourcePath = "/images/language.png";

        var resourceUrl = getClass().getResource(resourcePath);
        if (resourceUrl == null) {
            System.err.println("Warning: Image resource not found at " + resourcePath);
            return new Label("🌐");
        }

        Image image = new Image(resourceUrl.toExternalForm());
        ImageView imageView = new ImageView(image);

        // Adjust these values to match the dimensions of your PNG
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        imageView.setPreserveRatio(true);

        return imageView;
    }
}