package com.rutas.visual.controladores;

import com.rutas.logico.crud.ParadaCrud;
import com.rutas.logico.modelo.TipoParada;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistroParadaController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<TipoParada> cmbTipo;

    private ParadaCrud paradaCrud;

    public void setParadaCrud(ParadaCrud paradaCrud) {
        this.paradaCrud = paradaCrud;
    }

    @FXML
    public void initialize() {
        cmbTipo.getItems().addAll(TipoParada.values());
    }

    @FXML
    public void handleGuardar(ActionEvent e) {
        String nombre    = txtNombre.getText();
        String direccion = txtDireccion.getText();
        TipoParada tipo  = cmbTipo.getValue();

        paradaCrud.insertarParada(nombre, tipo, direccion);
        cerrar();
    }

    @FXML
    public void handleCancelar(ActionEvent e) {
        cerrar();
    }

    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
}