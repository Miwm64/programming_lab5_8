package ru.spb.miwm64.moviemanager.client.gui.dialog;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class MyDialog extends Dialog{
    private final DialogPane dialogPane;
    public MyDialog() {
        dialogPane = this.getDialogPane();
    }
}
