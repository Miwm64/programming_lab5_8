package com.example.pane;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class CreatePane extends Dialog {
    public CreatePane() {
        ButtonType applyButton = new ButtonType("Create", ButtonBar.ButtonData.APPLY);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().addAll(applyButton);
        this.getDialogPane().getButtonTypes().addAll(cancelButton);
    }
}
