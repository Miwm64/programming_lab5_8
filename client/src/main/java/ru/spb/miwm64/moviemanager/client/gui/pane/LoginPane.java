package ru.spb.miwm64.moviemanager.client.gui.pane;

import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class LoginPane extends VBox {
    private final Label titleLabel;
    private final TextField nicknameEdit;
    private final PasswordField passwordEdit;
    private final Button enterButton;
    private final Button switchButton;

    public LoginPane() {
        titleLabel = new Label();
        nicknameEdit = new TextField();
        passwordEdit = new PasswordField();

        enterButton = new Button();
        switchButton = new Button();

        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        elementInit();
    }

    private void elementInit(){
        nicknameEdit.setPromptText("nickname");
        nicknameEdit.setMaxWidth(300);
        nicknameEdit.setMaxHeight(200);
        nicknameEdit.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24;" +
                "-fx-border-color: E7B36F; -fx-border-width: 1; -fx-border-radius: 24;");
        nicknameEdit.setFont(Helper.getFont(14));

        passwordEdit.setPromptText("password");
        passwordEdit.setMaxWidth(300);
        passwordEdit.setMaxHeight(200);
        passwordEdit.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24; " +
                "-fx-border-color: E7B36F; -fx-border-width: 1; -fx-border-radius: 24;");
        passwordEdit.setFont(Helper.getFont(14));

        enterButton.setStyle("-fx-background-color: #D8AA7E; -fx-background-radius: 24px;");
        enterButton.setFont(Helper.getFont(16));
        enterButton.textProperty().bind(I18N.createBinding("login_pane.button.login"));

        switchButton.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24px;");
        switchButton.setFont(Helper.getFont(16));
        switchButton.setTextFill(Color.BLACK);
        switchButton.textProperty().bind(I18N.createBinding("login_pane.button.switch_to_registration"));

        titleLabel.textProperty().bind(I18N.createBinding("login_pane.label.title"));
        titleLabel.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 20px;");
        titleLabel.setFont(Helper.getBoldFont(18));
        titleLabel.setPadding(new Insets(0, 10, 0, 10));
        setMargin(titleLabel, new Insets(0, 0, 40, 0));
        setMargin(passwordEdit, new Insets(10, 0, 40, 0));
        setMargin(switchButton, new Insets(0, 0, 20, 0));

        this.setStyle("-fx-background-color: #DAC0A7;-fx-background-radius: 20px;");
        this.setMaxWidth(400);
        this.setMaxHeight(500);
        this.setAlignment(Pos.CENTER);

        this.getChildren().add(titleLabel);
        this.getChildren().add(nicknameEdit);
        this.getChildren().add(passwordEdit);

        this.getChildren().add(switchButton);
        this.getChildren().add(enterButton);
    }

    public Button getSwitchButton() {
        return switchButton;
    }

    public Button getLoginButton() {
        return enterButton;
    }

    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<>();
        data.put("username", nicknameEdit.getText());
        data.put("password", passwordEdit.getText());
        return data;
    }
}