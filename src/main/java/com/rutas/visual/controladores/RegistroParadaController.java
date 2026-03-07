package com.rutas.visual.controladores;

import com.rutas.logico.crud.ParadaCrud;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.TipoParada;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistroParadaController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<TipoParada> cmbTipo;

    private ParadaCrud paradaCrud;
    private Parada paradaAModificar = null;

    public void setParadaCrud(ParadaCrud paradaCrud) {
        this.paradaCrud = paradaCrud;
    }

    @FXML
    public void initialize() {
        cmbTipo.setItems(FXCollections.observableArrayList(TipoParada.values()));
    }

    public void setParada(Parada parada) {
        this.paradaAModificar = parada;
        lblTitulo.setText("Modificar Parada");
        txtNombre.setText(parada.getNombreParada());
        txtDireccion.setText(parada.getUbicacion());
        cmbTipo.setValue(parada.getTipo());
    }

    @FXML
    public void handleGuardar(ActionEvent e) {
        String nombre    = txtNombre.getText().trim();
        String direccion = txtDireccion.getText().trim();
        TipoParada tipo  = cmbTipo.getValue();

        if (nombre.isEmpty() || tipo == null) {
            mostrarAlerta("El nombre y el tipo de transporte son obligatorios.");
            return;
        }

        if (paradaAModificar != null) {
            paradaCrud.modificarParada(paradaAModificar.getCodigo(), nombre, tipo, direccion);
        } else {
            paradaCrud.insertarParada(nombre, tipo, direccion);
        }

        cerrar();
    }

    @FXML
    public void handleCancelar(ActionEvent e) {
        cerrar();
    }

    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}