package com.rutas.visual.controladores;

import com.rutas.logico.crud.ParadaCrud;
import com.rutas.logico.crud.RutaCrud;
import com.rutas.logico.excepciones.VistaNoCargadaException;
import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.Ruta;
import com.rutas.servicios.ServicioGrafo;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class RutasController {

    @FXML private TextField txtBuscar;
    @FXML private Label lblContador;
    @FXML private TableView<Ruta> tablaRutas;
    @FXML private TableColumn<Ruta, String> colId;
    @FXML private TableColumn<Ruta, String> colNombre;
    @FXML private TableColumn<Ruta, String> colOrigen;
    @FXML private TableColumn<Ruta, String> colDestino;
    @FXML private TableColumn<Ruta, String> colTiempo;
    @FXML private TableColumn<Ruta, String> colCosto;
    @FXML private TableColumn<Ruta, String> colDistancia;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;

    private final RutaCrud rutaCrud = new RutaCrud(ServicioGrafo.get());
    private final ParadaCrud paradaCrud = new ParadaCrud(ServicioGrafo.get());

    private ObservableList<Ruta> listaBase = FXCollections.observableArrayList();
    private FilteredList<Ruta> listaFiltrada;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colOrigen.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Ruta, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Ruta, String> c) {
                return new SimpleStringProperty(c.getValue().getOrigen().getNombreParada());
            }
        });

        colDestino.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Ruta, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Ruta, String> c) {
                return new SimpleStringProperty(c.getValue().getDestino().getNombreParada());
            }
        });

        colTiempo.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Ruta, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Ruta, String> c) {
                return new SimpleStringProperty(c.getValue().getPeso(Criterio.TIEMPO) + " min");
            }
        });

        colCosto.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Ruta, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Ruta, String> c) {
                return new SimpleStringProperty("$" + c.getValue().getPeso(Criterio.COSTO));
            }
        });

        colDistancia.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Ruta, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Ruta, String> c) {
                return new SimpleStringProperty(c.getValue().getPeso(Criterio.DISTANCIA) + " km");
            }
        });

        listaFiltrada = new FilteredList<>(listaBase);
        tablaRutas.setItems(listaFiltrada);

        for (TableColumn<?, ?> col : tablaRutas.getColumns()) {
            col.setResizable(false);
            col.setReorderable(false);
        }

        tablaRutas.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Ruta>() {
            @Override
            public void changed(ObservableValue<? extends Ruta> obs, Ruta oldVal, Ruta newVal) {
                boolean seleccionado = newVal != null;
                btnModificar.setDisable(!seleccionado);
                btnEliminar.setDisable(!seleccionado);
            }
        });

        txtBuscar.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs, String oldVal, String newVal) {
                aplicarFiltros();
            }
        });

        actualizarTabla();
    }

    /*
        Nombre: aplicarFiltros
        Argumentos: Ninguno.
        Objetivo: Filtrar la tabla de rutas según el texto ingresado en el campo de búsqueda,
                  comparando contra el ID, nombre, origen y destino de cada ruta.
        Retorno: (void) No retorna valor.
     */

    private void aplicarFiltros() {
        final String textoBusqueda = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();

        listaFiltrada.setPredicate(new Predicate<Ruta>() {
            @Override
            public boolean test(Ruta ruta) {
                if (textoBusqueda.isEmpty()) return true;
                return ruta.getId().toLowerCase().contains(textoBusqueda)
                        || ruta.getNombre().toLowerCase().contains(textoBusqueda)
                        || ruta.getOrigen().getNombreParada().toLowerCase().contains(textoBusqueda)
                        || ruta.getDestino().getNombreParada().toLowerCase().contains(textoBusqueda);
            }
        });

        lblContador.setText(listaFiltrada.size() + " rutas");
    }

    @FXML
    public void handleNuevaRuta(ActionEvent e) {
        abrirFormulario(null);
    }

    @FXML
    public void handleModificar(ActionEvent e) {
        Ruta seleccionada = tablaRutas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;
        abrirFormulario(seleccionada);
    }

    @FXML
    public void handleEliminar(ActionEvent e) {
        Ruta seleccionada = tablaRutas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Deseas eliminar la ruta \"" + seleccionada.getNombre() + "\"?");
        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            rutaCrud.eliminarRuta(seleccionada);
            actualizarTabla();
        }
    }

    /*
        Nombre: abrirFormulario
        Argumentos:
            (Ruta) ruta: Representa la ruta a modificar, o null si se desea registrar una nueva.
        Objetivo: Validar que existan al menos dos paradas y abrir el formulario modal de
                  registro o modificación de ruta, actualizando la tabla al cerrar.
        Retorno: (void) No retorna valor.
     */

    private void abrirFormulario(Ruta ruta) {
        try {
            // Validar ANTES de abrir la ventana
            if (paradaCrud.listarParadas().size() < 2) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Validación");
                alert.setHeaderText(null);
                alert.setContentText("Se necesitan al menos dos paradas para registrar una ruta.");
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegistroRuta.fxml"));
            Parent vista = loader.load();

            RegistroRutaController ctrl = loader.getController();
            ctrl.setRutaCrud(rutaCrud);
            ctrl.setParadas(paradaCrud.listarParadas());
            if (ruta != null) ctrl.setRuta(ruta);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(ruta != null ? "Modificar Ruta" : "Nueva Ruta");
            dialog.setScene(new Scene(vista));
            dialog.setResizable(false);
            dialog.centerOnScreen();
            dialog.showAndWait();

            actualizarTabla();

        } catch (Exception ex) {
            throw new VistaNoCargadaException("/view/RegistroRuta.fxml", ex);
        }
    }

    /*
        Nombre: actualizarTabla
        Argumentos: Ninguno.
        Objetivo: Recargar los datos de la tabla de rutas desde el grafo, reaplica los filtros
                  y restaura la selección previa si la ruta aún existe.
        Retorno: (void) No retorna valor.
     */

    private void actualizarTabla() {
        Ruta seleccionadaAntes = tablaRutas.getSelectionModel().getSelectedItem();
        List<Ruta> rutas = rutaCrud.listarRutas();
        listaBase.setAll(rutas);
        aplicarFiltros();

        if (seleccionadaAntes != null) {
            for (Ruta r : listaBase) {
                if (r.getId().equals(seleccionadaAntes.getId())) {
                    tablaRutas.getSelectionModel().select(r);
                    break;
                }
            }
        }

        boolean haySeleccion = tablaRutas.getSelectionModel().getSelectedItem() != null;
        btnModificar.setDisable(!haySeleccion);
        btnEliminar.setDisable(!haySeleccion);
    }
}