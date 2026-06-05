package ru.spb.miwm64.moviemanager.client.gui.widgets;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;

import java.util.Locale;
// TODO icon
public class LanguageSelector extends ComboBox<String> {
    private final ObservableList<String> langs;
    public LanguageSelector() {
        langs = FXCollections.observableArrayList();
        langs.add("English");
        langs.add("Français");
        langs.add("Svenska");
        langs.add("Español (Spain)");
        this.setItems(langs);
        this.getSelectionModel().select(0);
        this.valueProperty().addListener((observable, oldValue, newValue) -> {
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
                default: {
                    I18N.setLocale(Locale.ENGLISH);
                }
            }
        });
    }
}
