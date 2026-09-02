package ru.spb.miwm64.moviemanager.client.gui.pane;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;


public class EasterEggPane extends StackPane {
    public EasterEggPane() {
        var url = getClass().getResource("/images/image.gif");
        String mediaUrl = url.toExternalForm();
        Image image = new Image(mediaUrl);


        // simple displays ImageView the image as is
        ImageView iv1 = new ImageView();
        iv1.setImage(image);

        this.getChildren().add(iv1);
    }
}
