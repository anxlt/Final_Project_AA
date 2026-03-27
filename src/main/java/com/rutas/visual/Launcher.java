package com.rutas.visual;

import com.rutas.logico.database.DatabaseInitializer;
import com.rutas.logico.database.repositorios.ParadaRepositorio;
import com.rutas.logico.database.repositorios.RutaRepositorio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;
import com.rutas.servicios.ServicioGrafo;
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
        DatabaseInitializer.inicializar();
        GrafoTransporte grafo = new GrafoTransporte();
        cargarDatosDesdeDB(grafo);
        ServicioGrafo.init(grafo);

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

    private void cargarDatosDesdeDB(GrafoTransporte grafo) {
        ParadaRepositorio paradaRepo = new ParadaRepositorio();
        RutaRepositorio rutaRepo = new RutaRepositorio();

        for (Parada p : paradaRepo.listarTodas()) {
            grafo.agregarParada(p);
        }

        for (Ruta r : rutaRepo.listarTodas()) {
            grafo.agregarRuta(r);
        }
    }
}