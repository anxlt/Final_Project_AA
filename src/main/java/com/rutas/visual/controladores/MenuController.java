package com.rutas.visual.controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class MenuController {

    @FXML private BorderPane borderPane;
    @FXML private Button btnPrincipal;
    @FXML private Button btnParadas;
    @FXML private Button btnRutas;
    @FXML private Button btnMapa;

    private Button botonActivo;

    @FXML
    public void initialize() {
        botonActivo = btnPrincipal;
        cargarVista("/view/Principal.fxml");
    }

    @FXML
    public void handlePrincipal(ActionEvent e) {
        cambiarBoton(btnPrincipal);
        cargarVista("/view/Principal.fxml");
    }

    @FXML
    public void handleParadas(ActionEvent e) {
        cambiarBoton(btnParadas);
        cargarVista("/view/Paradas.fxml");
    }

    @FXML
    public void handleRutas(ActionEvent e) {
        cambiarBoton(btnRutas);
        cargarVista("/view/Rutas.fxml");
    }

    @FXML
    public void handleMapa(ActionEvent e) {
        cambiarBoton(btnMapa);
        cargarVista("/view/Mapa.fxml");
    }

    private void cambiarBoton(Button nuevo) {
        String inactivo = "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 16px; -fx-padding: 16 20; -fx-alignment: CENTER-LEFT; -fx-cursor: hand; -fx-font-weight: normal;";
        String activo = "-fx-background-color: #234C6A; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 16 20; -fx-alignment: CENTER-LEFT; -fx-background-radius: 8; -fx-cursor: hand;";

        botonActivo.setStyle(inactivo);
        nuevo.setStyle(activo);
        botonActivo = nuevo;
    }

    private void cargarVista(String ruta) {
        try {
            AnchorPane vista = FXMLLoader.load(getClass().getResource(ruta));
            borderPane.setCenter(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}