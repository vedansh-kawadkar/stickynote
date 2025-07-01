package com.stickynote.app;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    public void makeResizable(Stage stage, Scene scene) {
        final int RESIZE_MARGIN = 8;

        scene.setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();
            double width = stage.getWidth();
            double height = stage.getHeight();

            if (x >= width - RESIZE_MARGIN && y >= height - RESIZE_MARGIN) {
                scene.setCursor(Cursor.SE_RESIZE);
            } else if (x >= width - RESIZE_MARGIN) {
                scene.setCursor(Cursor.E_RESIZE);
            } else if (y >= height - RESIZE_MARGIN) {
                scene.setCursor(Cursor.S_RESIZE);
            } else {
                scene.setCursor(Cursor.DEFAULT);
            }
        });

        scene.setOnMousePressed(event -> {
            xOffset = event.getX();
            yOffset = event.getY();
        });

        scene.setOnMouseDragged(event -> {
            Cursor cursor = scene.getCursor();
            if (cursor == Cursor.SE_RESIZE) {
                stage.setWidth(event.getX());
                stage.setHeight(event.getY());
            } else if (cursor == Cursor.E_RESIZE) {
                stage.setWidth(event.getX());
            } else if (cursor == Cursor.S_RESIZE) {
                stage.setHeight(event.getY());
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlLocation = getClass().getResource("/primary.fxml");
            if (fxmlLocation == null) {
                throw new IOException("FXML file not found at primary.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(loader.load(), 500, 400);

            URL cssURL = getClass().getResource("/styles.css");
            if (cssURL != null) {
                scene.getStylesheets().add(cssURL.toExternalForm());
            }

            PrimaryController controller = loader.getController();
            controller.setFileName(null);
            controller.setStage(primaryStage);

            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Sticky Note");
            primaryStage.setAlwaysOnTop(false);
            primaryStage.setResizable(true);
            makeResizable(primaryStage, scene);

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
