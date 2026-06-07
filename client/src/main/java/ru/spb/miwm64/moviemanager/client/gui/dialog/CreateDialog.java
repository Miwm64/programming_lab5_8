package ru.spb.miwm64.moviemanager.client.gui.dialog;

import javafx.scene.layout.VBox;
import ru.spb.miwm64.moviemanager.client.commands.AddCommand;

public final class CreateDialog extends MyDialog {
    public CreateDialog() {
        super();
        titleLabel.setText("Create Movie");
        parameterList = new AddCommand(null).getParams();

        for (var param : parameterList) {
            addField(param);
        }
    }
}
