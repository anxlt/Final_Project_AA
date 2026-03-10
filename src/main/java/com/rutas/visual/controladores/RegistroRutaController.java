package com.rutas.visual.controladores;

import com.rutas.logico.crud.RutaCrud;
import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;
import com.rutas.servicios.ServicioGrafo;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class RegistroRutaController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;
    @FXML private Spinner<Double> spnTiempo;
    @FXML private Spinner<Double> spnCosto;
    @FXML private Spinner<Double> spnDistancia;
    @FXML private Spinner<Integer> spnTransbordos;

    private RutaCrud rutaCrud;
    private Ruta rutaAModificar = null;

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
        cmbOrigen.setItems(FXCollections.observableArrayList(paradas));
        cmbDestino.setItems(FXCollections.observableArrayList(paradas));
    }

    public void setRuta(Ruta ruta) {
        this.rutaAModificar = ruta;
        lblTitulo.setText("Modificar Ruta");

        txtNombre.setText(ruta.getNombre());
        cmbOrigen.setValue(ruta.getOrigen());
        cmbDestino.setValue(ruta.getDestino());

        Object tiempo      = ruta.getPeso(Criterio.TIEMPO);
        Object costo       = ruta.getPeso(Criterio.COSTO);
        Object distancia   = ruta.getPeso(Criterio.DISTANCIA);
        Object transbordos = ruta.getPeso(Criterio.TRANSBORDOS);

        if (tiempo      instanceof Number) spnTiempo.getValueFactory().setValue(((Number) tiempo).doubleValue());
        if (costo       instanceof Number) spnCosto.getValueFactory().setValue(((Number) costo).doubleValue());
        if (distancia   instanceof Number) spnDistancia.getValueFactory().setValue(((Number) distancia).doubleValue());
        if (transbordos instanceof Number) spnTransbordos.getValueFactory().setValue(((Number) transbordos).intValue());
    }

    @FXML
    public void handleGuardar(ActionEvent e) {
        String nombre  = txtNombre.getText().trim();
        Parada origen  = cmbOrigen.getValue();
        Parada destino = cmbDestino.getValue();

        if(nombre.isBlank()){
            mostrarAlerta("Debes ingresar un nombre para la ruta");
            return;
        }

        if (origen == null || destino == null) {
            mostrarAlerta("Debes seleccionar origen y destino.");
            return;
        }

        if (origen.equals(destino)) {
            mostrarAlerta("El origen y el destino no pueden ser la misma parada.");
            return;
        }

        double tiempo      = spnTiempo.getValue();
        double costo       = spnCosto.getValue();
        double distancia   = spnDistancia.getValue();
        int    transbordos = spnTransbordos.getValue();

        if (rutaAModificar != null) {
            GrafoTransporte copia = ServicioGrafo.get().copiar();
            copia.eliminarRuta(rutaAModificar);
            Ruta rutaSimulada = new Ruta(rutaAModificar.getId(), nombre, origen, destino);
            for (Criterio c : Criterio.values()) {
                rutaSimulada.setPeso(c, rutaAModificar.getPeso(c));
            }
            copia.agregarRuta(rutaSimulada);
            Parada paradaProblema = copia.esConexo();
            if (paradaProblema != null) {
                mostrarAlerta("No se puede aplicar esta modificación porque el grafo quedaría no conexo: "
                        + " por la parada \"" + paradaProblema.getNombreParada() + "\"");
                return;
            }
            rutaCrud.modificarRuta(rutaAModificar, nombre, origen, destino, tiempo, costo, distancia, transbordos);
        } else {
            boolean agregado = rutaCrud.agregarRuta(nombre, origen, destino, tiempo, costo, distancia, transbordos);
            if (!agregado) {
                mostrarAlerta("Ya existe una ruta entre " + origen.getNombreParada()
                        + " y " + destino.getNombreParada() + ".");
                return;
            }
        }

        cerrarVentana();
    }

    @FXML
    public void handleCancelar(ActionEvent e) {
        cerrarVentana();
    }

    /*
        Nombre: cerrarVentana
        Argumentos: Ninguno.
        Objetivo: Cerrar la ventana modal del formulario de registro de ruta.
        Retorno: (void) No retorna valor.
     */

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    /*
        Nombre: mostrarAlerta
        Argumentos:
            (String) mensaje: Representa el contenido del mensaje de validación a mostrar.
        Objetivo: Mostrar un diálogo de advertencia con un mensaje de validación al usuario.
        Retorno: (void) No retorna valor.
     */

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}