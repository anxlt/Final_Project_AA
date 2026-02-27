package com.rutas.visual;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Principal extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Gestión de Rutas de Transporte");
        VBox root = new VBox(label);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Rutas de Transporte");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}