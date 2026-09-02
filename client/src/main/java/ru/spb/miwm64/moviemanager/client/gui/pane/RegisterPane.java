package ru.spb.miwm64.moviemanager.client.gui.pane;

import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
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
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterPane extends VBox {
    private final Label titleLabel;
    private final TextField nicknameEdit;
    private final TextField emailEdit;
    private final PasswordField passwordEdit;
    private final PasswordField password2Edit;
    private final Button enterButton;
    private final Button switchButton;

    public RegisterPane() {
        titleLabel = new Label();
        nicknameEdit = new TextField();
        emailEdit = new TextField();
        passwordEdit = new PasswordField();
        password2Edit = new PasswordField();

        enterButton = new Button();
        switchButton = new Button();

        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        elementInit();
    }

    private void elementInit(){
        nicknameEdit.promptTextProperty().bind(I18N.createBinding("register_pane.prompt.nickname"));
        nicknameEdit.setMaxWidth(300);
        nicknameEdit.setMaxHeight(200);
        nicknameEdit.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24;" +
                "-fx-border-color: E7B36F; -fx-border-width: 1; -fx-border-radius: 24;");
        nicknameEdit.setFont(Helper.getFont(14));

        emailEdit.promptTextProperty().bind(I18N.createBinding("register_pane.prompt.email"));
        emailEdit.setMaxWidth(300);
        emailEdit.setMaxHeight(200);
        emailEdit.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24;" +
                "-fx-border-color: E7B36F; -fx-border-width: 1; -fx-border-radius: 24;");
        emailEdit.setFont(Helper.getFont(14));

        passwordEdit.promptTextProperty().bind(I18N.createBinding("register_pane.prompt.password"));
        passwordEdit.setMaxWidth(300);
        passwordEdit.setMaxHeight(200);
        passwordEdit.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24; " +
                "-fx-border-color: E7B36F; -fx-border-width: 1; -fx-border-radius: 24;");
        passwordEdit.setFont(Helper.getFont(14));

        password2Edit.promptTextProperty().bind(I18N.createBinding("register_pane.prompt.repeat_password"));
        password2Edit.setMaxWidth(300);
        password2Edit.setMaxHeight(200);
        password2Edit.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24; " +
                "-fx-border-color: E7B36F; -fx-border-width: 1; -fx-border-radius: 24;");
        password2Edit.setFont(Helper.getFont(14));

        enterButton.setStyle("-fx-background-color: #D8AA7E; -fx-background-radius: 24px;");
        enterButton.setFont(Helper.getFont(16));
        enterButton.setDisable(true);
        enterButton.textProperty().bind(I18N.createBinding("register_pane.button.register"));

        switchButton.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 24px;");
        switchButton.setFont(Helper.getFont(16));
        switchButton.setTextFill(Color.BLACK);
        switchButton.textProperty().bind(I18N.createBinding("register_pane.button.switch_to_login"));

        titleLabel.textProperty().bind(I18N.createBinding("register_pane.label.title"));
        titleLabel.setStyle("-fx-background-color: #EEDEC5; -fx-background-radius: 20px;");
        titleLabel.setFont(Helper.getBoldFont(18));
        titleLabel.setPadding(new Insets(0, 10, 0, 10));
        setMargin(titleLabel, new Insets(0, 0, 40, 0));
        setMargin(emailEdit, new Insets(10, 0, 0, 0));
        setMargin(passwordEdit, new Insets(10, 0, 0, 0));
        setMargin(password2Edit, new Insets(10, 0, 40, 0));
        setMargin(switchButton, new Insets(0, 0, 20, 0));

        nicknameEdit.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(password2Edit.getText(), passwordEdit.getText()) || passwordEdit.getText().isEmpty() ||
                    nicknameEdit.getText().isEmpty() || emailEdit.getText().isEmpty() || !emailEdit.getText().contains("@")) {
                enterButton.setDisable(true);
            }
            else  {
                enterButton.setDisable(false);
            }
        });
        emailEdit.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(password2Edit.getText(), passwordEdit.getText()) || passwordEdit.getText().isEmpty() ||
                    nicknameEdit.getText().isEmpty() || emailEdit.getText().isEmpty() || !emailEdit.getText().contains("@")) {
                enterButton.setDisable(true);
            }
            else  {
                enterButton.setDisable(false);
            }
        });
        passwordEdit.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(password2Edit.getText(), newValue) || newValue.isEmpty() ||
                    nicknameEdit.getText().isEmpty() || emailEdit.getText().isEmpty() || !emailEdit.getText().contains("@")) {
                enterButton.setDisable(true);
            }
            else  {
                enterButton.setDisable(false);
            }
        });
        password2Edit.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(passwordEdit.getText(), newValue) || newValue.isEmpty() ||
                    nicknameEdit.getText().isEmpty() || emailEdit.getText().isEmpty() || !emailEdit.getText().contains("@")) {
                enterButton.setDisable(true);
            }
            else  {
                enterButton.setDisable(false);
            }
        });

        this.setStyle("-fx-background-color: #DAC0A7;-fx-background-radius: 20px;");
        this.setMaxWidth(400);
        this.setMaxHeight(500);
        this.setAlignment(Pos.CENTER);

        this.getChildren().add(titleLabel);
        this.getChildren().add(nicknameEdit);
        this.getChildren().add(emailEdit);
        this.getChildren().add(passwordEdit);
        this.getChildren().add(password2Edit);

        this.getChildren().add(switchButton);
        this.getChildren().add(enterButton);
    }

    public Button getSwitchButton() {
        return switchButton;
    }

    public Button getRegisterButton() {
        return enterButton;
    }

    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<>();
        data.put("username", nicknameEdit.getText());
        data.put("password", passwordEdit.getText());
        data.put("email", emailEdit.getText());
        data.put("firstName", "default");
        data.put("lastName", "default");
        return data;
    }
}