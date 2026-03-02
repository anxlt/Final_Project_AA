package com.rutas.visual;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.awt.*;
import java.io.IOException;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/view/Menu.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Sistema de Gestión de Rutas de Transporte");
        Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
        stage.getIcons().add(icon);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        stage.setWidth(screenSize.getWidth() - 20);
        stage.setHeight(screenSize.getHeight() - 50);
        stage.setResizable(false);

        stage.setScene(scene);
        stage.show();
    }
}