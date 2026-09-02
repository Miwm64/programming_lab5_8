package ru.spb.miwm64.moviemanager.client.gui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;

public class ConfirmationDialog extends Dialog<Boolean> {

    public ConfirmationDialog(String question, String title) {
        setTitle(title);
        setHeaderText(null);

        // Style the dialog pane
        getDialogPane().setStyle("-fx-background-color: #EEDEC5;");

        // Create question label
        Label questionLabel = new Label(question);
        questionLabel.setWrapText(true);
        questionLabel.setFont(Helper.getFont(14));
        questionLabel.setStyle("-fx-text-fill: black;");
        questionLabel.setAlignment(Pos.CENTER);

        VBox content = new VBox(questionLabel);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        getDialogPane().setContent(content);

        // Add Yes/No buttons with externalized text
        ButtonType yesButton = new ButtonType(I18N.get("confirmation_dialog.button.yes"));
        ButtonType noButton = new ButtonType(I18N.get("confirmation_dialog.button.no"));
        getDialogPane().getButtonTypes().addAll(yesButton, noButton);

        // Style the buttons after the dialog is shown
        Platform.runLater(() -> {
            Button yesBtn = (Button) getDialogPane().lookupButton(yesButton);
            Button noBtn = (Button) getDialogPane().lookupButton(noButton);
            String buttonStyle = "-fx-background-color: #DAC0A7; -fx-background-radius: 24px; " +
                    "-fx-border-radius: 24; -fx-font-size: 14px;";
            if (yesBtn != null) yesBtn.setStyle(buttonStyle);
            if (noBtn != null) noBtn.setStyle(buttonStyle);
        });

        // Convert result to boolean
        setResultConverter(button -> button == yesButton);
    }
}