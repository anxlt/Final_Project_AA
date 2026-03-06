package com.rutas.visual.controladores;

import com.rutas.logico.crud.RutaCrud;
import com.rutas.logico.modelo.Parada;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class RegistroRutaController {

    @FXML private TextField txtNombre;
    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;
    @FXML private Spinner<Double> spnTiempo;
    @FXML private Spinner<Double> spnCosto;
    @FXML private Spinner<Double> spnDistancia;
    @FXML private Spinner<Integer> spnTransbordos;

    private RutaCrud rutaCrud;

    public void setRutaCrud(RutaCrud rutaCrud) {
        this.rutaCrud = rutaCrud;
    }

    @FXML
    public void initialize() {
        spnTiempo.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1440, 0, 1));
        spnCosto.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100000, 0, 1));
        spnDistancia.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 99999, 0, 0.5));
        spnTransbordos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0, 1));
    }

    public void setParadas(List<Parada> paradas) {
        if (paradas.size() < 2) {
            mostrarAlerta("Se necesitan al menos dos paradas para registrar una ruta.");
            cerrarVentana();
            return;
        }
        cmbOrigen.setItems(FXCollections.observableArrayList(paradas));
        cmbDestino.setItems(FXCollections.observableArrayList(paradas));
    }

    @FXML
    public void handleGuardar(ActionEvent e) {
        String nombre = txtNombre.getText().trim();
        Parada origen = cmbOrigen.getValue();
        Parada destino = cmbDestino.getValue();
        double tiempo = spnTiempo.getValue();
        double costo = spnCosto.getValue();
        double distancia = spnDistancia.getValue();
        int transbordos = spnTransbordos.getValue();

        rutaCrud.agregarRuta(nombre, origen, destino, tiempo, costo, distancia, transbordos);
        cerrarVentana();
    }

    @FXML
    public void handleCancelar(ActionEvent e) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
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