package ru.spb.miwm64.moviemanager.client.gui.util;

import javafx.scene.control.Alert;
// TODO translations
public class GuiFactory {
    public static Alert createInfoPopup(String text) {
        Alert popup = new Alert(Alert.AlertType.INFORMATION);
        popup.setContentText(text);
        return popup;
    }

    public static Alert createErrorPopup(String text) {
        Alert popup = new Alert(Alert.AlertType.ERROR);
        popup.setContentText(text);
        return popup;
    }
}
