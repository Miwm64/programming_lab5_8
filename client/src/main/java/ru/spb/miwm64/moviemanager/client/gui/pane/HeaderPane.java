package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.spb.miwm64.moviemanager.client.gui.util.Helper;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import ru.spb.miwm64.moviemanager.client.gui.widgets.LanguageSelector;

public class HeaderPane extends StackPane {

    private final Label title;
    private final LanguageSelector languageSelector;
    private final HBox leftLayout;
    private final HBox rightLayout;
    private final Button switchButton;
    private final Button logoutButton;
    private final Label nicknameLabel;
    private final BooleanProperty isMapView = new SimpleBooleanProperty(false);

    public HeaderPane(Stage primaryStage) {
        title = new Label();
        languageSelector = new LanguageSelector();
        leftLayout = new HBox();
        rightLayout = new HBox();
        switchButton = new Button();
        logoutButton = new Button();
        nicknameLabel = new Label();

        // Bind button text dynamically
        switchButton.textProperty().bind(Bindings.createStringBinding(
                () -> isMapView.get()
                        ? I18N.get("header_pane.button.switch_to_table")
                        : I18N.get("header_pane.button.switch_to_map"),
                isMapView, I18N.localeProperty()
        ));

        initLayout();
        bindTitleText();
        applyStyles();
    }

    private void initLayout() {
        BorderPane headerLayout = new BorderPane();

        headerLayout.setLeft(leftLayout);
        headerLayout.setCenter(title);
        headerLayout.setRight(rightLayout);

        rightLayout.prefWidthProperty().bind(leftLayout.widthProperty());
        rightLayout.minWidthProperty().bind(leftLayout.widthProperty());

        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);

        this.getChildren().add(headerLayout);

        switchButton.setStyle("-fx-background-color: #DAC0A7; -fx-background-radius: 24px;");
        rightLayout.setStyle("-fx-background-color: #DAC0A7;-fx-background-radius: 20px;");
        rightLayout.setAlignment(Pos.CENTER);
        switchButton.setFont(Helper.getFont(18));
        switchButton.setTextFill(Color.BLACK);

        rightLayout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        leftLayout.getChildren().add(switchButton);
        rightLayout.getChildren().add(languageSelector);
        rightLayout.getChildren().add(nicknameLabel);
        rightLayout.getChildren().add(logoutButton);
        nicknameLabel.setFont(Helper.getBoldFont(18));

        String resourcePath = "/images/logout.png";
        var resourceUrl = getClass().getResourceAsStream(resourcePath);
        Image icon = new Image(resourceUrl);
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        imageView.setPreserveRatio(true);
        logoutButton.setStyle("-fx-background-color: transparent;");
        logoutButton.setGraphic(imageView);

        HBox.setMargin(leftLayout, new Insets(0,0,0, 10));
        HBox.setMargin(rightLayout, new Insets(0,10,0, 0));
        HBox.setMargin(nicknameLabel, new Insets(0,10,0,0));
        rightLayout.setPadding(new Insets(0, 10, 0, 10));
        leftLayout.setPadding(new Insets(0, 10, 0, 10));
    }

    private void bindTitleText() {
        title.textProperty().bind(I18N.createBinding("header_pane.label.title"));
    }

    private void applyStyles() {
        title.setStyle("-fx-background-color: #DAC0A7; -fx-background-radius: 20px;");
        title.setTextFill(Color.web("#A100FF"));
        title.setFont(Helper.getBoldFont(20));
        title.setPadding(new Insets(0, 100, 0, 100));
        BorderPane.setAlignment(title, Pos.CENTER);
    }

    public Button getLogoutButton() {
        return logoutButton;
    }

    public void hideTop(){
        leftLayout.setVisible(false);
        rightLayout.setVisible(false);
    }

    public void showTop(){
        leftLayout.setVisible(true);
        rightLayout.setVisible(true);
    }

    public void setNickname(String nickname) {
        nicknameLabel.setText(nickname);
    }

    public Button getSwitchButton() {
        return switchButton;
    }

    public void setMapView(boolean isMap) {
        isMapView.set(isMap);
    }
}