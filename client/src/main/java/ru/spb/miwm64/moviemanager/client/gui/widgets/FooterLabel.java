package ru.spb.miwm64.moviemanager.client.gui.widgets;

import javafx.scene.control.Label;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;

public class FooterLabel extends Label {
    public FooterLabel() {
        this.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        this.textProperty().bind(I18N.createBinding("developed_by"));
    }
}
