package com.izzaz.appdrawer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class AppDrawerMain extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppDrawerMain.class.getResource("main.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        AppDrawerController controller = (AppDrawerController)fxmlLoader.getController();
        controller.setStageAndSetupListeners(stage);

    }

    public static void main(String[] args) {
        launch();
    }
}
