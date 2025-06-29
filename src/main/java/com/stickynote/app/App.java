package com.stickynote.app;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlLocation = getClass().getResource("/com/stickynote/app/primary.fxml");
            if (fxmlLocation == null) {
                throw new IOException("FXML file not found at /com/stickynote/app/primary.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(loader.load(), 500, 400);

            URL cssURL = getClass().getResource("/styles.css");
            if (cssURL != null) {
                scene.getStylesheets().add(cssURL.toExternalForm());
            }

            PrimaryController controller = loader.getController();
            controller.setFileName(null); // This will create a new untitled note
            controller.setStage(primaryStage);

            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Sticky Note");
            primaryStage.setAlwaysOnTop(false);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
