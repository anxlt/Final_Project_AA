package com.rutas.visual.controladores;

import com.rutas.logico.crud.RutaCrud;
import com.rutas.logico.modelo.Parada;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class RegistroRutaController {

    @FXML private TextField txtId;
    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;
    @FXML private TextField txtTiempo;
    @FXML private TextField txtCosto;
    @FXML private TextField txtDistancia;
    @FXML private TextField txtTransbordos;

    private RutaCrud rutaCrud;

    public void setRutaCrud(RutaCrud rutaCrud) {
        this.rutaCrud = rutaCrud;
    }

    public void setParadas(List<Parada> paradas) {
        cmbOrigen.setItems(FXCollections.observableArrayList(paradas));
        cmbDestino.setItems(FXCollections.observableArrayList(paradas));
    }

    @FXML
    public void handleGuardar(ActionEvent e) {
        String id = txtId.getText().trim();
        Parada origen = cmbOrigen.getValue();
        Parada destino = cmbDestino.getValue();
        double tiempo = Double.parseDouble(txtTiempo.getText().trim());
        double costo = Double.parseDouble(txtCosto.getText().trim());
        double distancia = Double.parseDouble(txtDistancia.getText().trim());
        int transbordos = Integer.parseInt(txtTransbordos.getText().trim());

        rutaCrud.agregarRuta(id, origen, destino, tiempo, costo, distancia, transbordos);
        cerrarVentana();
    }

    @FXML
    public void handleCancelar(ActionEvent e) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtId.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}