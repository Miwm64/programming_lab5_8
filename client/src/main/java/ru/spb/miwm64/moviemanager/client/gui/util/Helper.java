package ru.spb.miwm64.moviemanager.client.gui.util;

import javafx.scene.text.Font;

public class Helper {
    public static Font getBoldFont(int size){
        return Font.loadFont(Helper.class.getResourceAsStream("/fonts/InknutAntiqua-Bold.ttf"), size);
    }

    public static Font getFont(int size){
        return Font.loadFont(Helper.class.getResourceAsStream("/fonts/InknutAntiqua-Regular.ttf"), size);
    }
}
