package com.rutas.visual.controladores;

import com.rutas.logico.crud.ParadaCrud;
import com.rutas.logico.excepciones.VistaNoCargadaException;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.TipoParada;
import com.rutas.servicios.ServicioGrafo;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ParadasController {

    @FXML private ComboBox<TipoParada> cmbTipo;
    @FXML private Label lblContador;
    @FXML private TableView<Parada> tablaParadas;
    @FXML private TableColumn<Parada, String> colId;
    @FXML private TableColumn<Parada, String> colNombre;
    @FXML private TableColumn<Parada, String> colTipo;
    @FXML private TableColumn<Parada, String> colDireccion;

    private final ParadaCrud paradaCrud = new ParadaCrud(ServicioGrafo.get());

    @FXML
    public void initialize() {
        cmbTipo.getItems().add(null);
        cmbTipo.getItems().addAll(TipoParada.values());
        cmbTipo.getSelectionModel().selectFirst();

        colId.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreParada"));
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipo().getNombre()));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));

        actualizarTabla();
    }

    @FXML
    public void handleNuevaParada(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegistroParada.fxml"));
            Parent vista = loader.load();

            RegistroParadaController ctrl = loader.getController();
            ctrl.setParadaCrud(paradaCrud);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Nueva Parada");
            dialog.setScene(new Scene(vista));
            dialog.getIcons().add(new Image(getClass().getResourceAsStream("/images/regParadaPin.png")));
            dialog.setResizable(false);
            dialog.centerOnScreen();
            dialog.showAndWait();

            actualizarTabla();

        } catch (Exception ex) {
            throw new VistaNoCargadaException("/view/RegistroParada.fxml", ex);
        }
    }

    private void actualizarTabla() {
        List<Parada> paradas = paradaCrud.listarParadas();
        tablaParadas.setItems(FXCollections.observableArrayList(paradas));
        lblContador.setText("Total: " + paradas.size());
    }
}