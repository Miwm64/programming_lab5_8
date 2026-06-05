package ru.spb.miwm64.moviemanager.client.gui.pane;

import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class CreatePane extends Dialog {
    public CreatePane() {
        ButtonType applyButton = new ButtonType("", ButtonBar.ButtonData.APPLY);
        ButtonType cancelButton = new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().addAll(applyButton, cancelButton);

        // Bind button texts after dialog is shown
        setOnShown(event -> {
            javafx.scene.control.Button btnApply = (javafx.scene.control.Button) getDialogPane().lookupButton(applyButton);
            javafx.scene.control.Button btnCancel = (javafx.scene.control.Button) getDialogPane().lookupButton(cancelButton);
            btnApply.textProperty().bind(I18N.createBinding("create_pane.button.create"));
            btnCancel.textProperty().bind(I18N.createBinding("create_pane.button.cancel"));
        });
    }
}