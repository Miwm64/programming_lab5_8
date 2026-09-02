package ru.spb.miwm64.moviemanager.client.gui.dialog;

import javafx.application.Platform;
import javafx.scene.layout.VBox;
import ru.spb.miwm64.moviemanager.client.command.Command;
import ru.spb.miwm64.moviemanager.client.command.Parameter;
import ru.spb.miwm64.moviemanager.client.commands.AddCommand;
import ru.spb.miwm64.moviemanager.client.commands.UpdateByIDCommand;
import ru.spb.miwm64.moviemanager.client.gui.util.GuiFactory;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;

public final class CreateDialog extends MyDialog {
    private final CollectionManager collectionManager;
    public CreateDialog(CollectionManager collectionManager) {
        super();
        this.collectionManager = collectionManager;
        titleLabel.setText(I18N.get("create_dialog.title"));
        parameterList = new AddCommand(null).getParams();

        for (var param : parameterList) {
            addField(param);
        }

    }

    @Override
    protected void execute() {
        Platform.runLater(() -> {
            Command createCommand = new AddCommand(collectionManager);
            createCommand.setParams(parameterList);
            var res = createCommand.execute();
            if (!res.isSuccess()) {
                GuiFactory.createErrorPopupWithProperty("create_dialog.error.could_not_create").show();
            }
        });
    }
}