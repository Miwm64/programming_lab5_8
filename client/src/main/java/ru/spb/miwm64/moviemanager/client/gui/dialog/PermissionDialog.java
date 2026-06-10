package ru.spb.miwm64.moviemanager.client.gui.dialog;

import javafx.application.Platform;
import ru.spb.miwm64.moviemanager.client.command.Command;
import ru.spb.miwm64.moviemanager.client.commands.AddCommand;
import ru.spb.miwm64.moviemanager.client.commands.GrantAccessCommand;
import ru.spb.miwm64.moviemanager.client.commands.RevokeAccessCommand;
import ru.spb.miwm64.moviemanager.client.gui.util.GuiFactory;
import ru.spb.miwm64.moviemanager.client.net.JsonRpcClient;

public final class PermissionDialog extends MyDialog {
    private final boolean isGrant;
    private final JsonRpcClient rpcClient;
    public PermissionDialog(boolean isGrant, JsonRpcClient client) {
        super();
        this.isGrant = isGrant;
        this.rpcClient = client;

        if (isGrant) {
            titleLabel.setText("Access grant");
        }
        else {
            titleLabel.setText("Access revoke");
        }
        parameterList = new GrantAccessCommand(null).getParams();

        for (var param : parameterList) {
            addField(param);
        }
    }

    @Override
    protected void execute() {
        Platform.runLater(() -> {
            Command command;
            if  (isGrant) {
                command = new GrantAccessCommand(this.rpcClient);
            }
            else {
                command = new RevokeAccessCommand(this.rpcClient);

            }
            command.setParams(parameterList);
            var res = command.execute();
            if (!res.isSuccess()) {
                GuiFactory.createErrorPopup("Internet connection error");
            }
        });
    }
}
