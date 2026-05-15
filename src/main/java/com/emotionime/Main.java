package com.emotionime;

import com.emotionime.ui.AppUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.setProperty("prism.lcdtext", "true");
        System.setProperty("prism.text", "t2k");
        new AppUI(primaryStage);
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "true");
        System.setProperty("prism.text", "t2k");
        launch(args);
    }
}
