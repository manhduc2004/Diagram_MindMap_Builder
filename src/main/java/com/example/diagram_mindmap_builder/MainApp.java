package com.example.diagram_mindmap_builder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/diagram_mindmap_builder/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 800);
        primaryStage.setTitle("Diagram/MindMap Builder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
