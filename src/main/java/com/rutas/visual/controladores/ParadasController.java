package com.rutas.visual.controladores;

import com.rutas.logico.crud.ParadaCrud;
import com.rutas.logico.excepciones.VistaNoCargadaException;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.TipoParada;
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
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ParadasController {

    @FXML private ComboBox<TipoParada> cmbTipo;
    @FXML private TextField txtBuscar;
    @FXML private Label lblContador;
    @FXML private TableView<Parada> tablaParadas;
    @FXML private TableColumn<Parada, String> colId;
    @FXML private TableColumn<Parada, String> colNombre;
    @FXML private TableColumn<Parada, String> colTipo;
    @FXML private TableColumn<Parada, String> colDireccion;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;

    private final ParadaCrud paradaCrud = new ParadaCrud(ServicioGrafo.get());
    private ObservableList<Parada> listaBase = FXCollections.observableArrayList();
    private FilteredList<Parada> listaFiltrada;

    @FXML
    public void initialize() {
        cmbTipo.getItems().add(null);
        cmbTipo.getItems().addAll(TipoParada.values());
        cmbTipo.getSelectionModel().selectFirst();

        cmbTipo.setButtonCell(new ListCell<TipoParada>() {
            @Override
            protected void updateItem(TipoParada item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "Todos los tipos" : item.getNombre());
            }
        });

        cmbTipo.setCellFactory(new Callback<ListView<TipoParada>, ListCell<TipoParada>>() {
            @Override
            public ListCell<TipoParada> call(ListView<TipoParada> lv) {
                return new ListCell<TipoParada>() {
                    @Override
                    protected void updateItem(TipoParada item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item == null ? "Todos los tipos" : item.getNombre());
                    }
                };
            }
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreParada"));

        colTipo.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Parada, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Parada, String> c) {
                return new SimpleStringProperty(c.getValue().getTipo().getNombre());
            }
        });

        colDireccion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));

        listaFiltrada = new FilteredList<>(listaBase);
        tablaParadas.setItems(listaFiltrada);

        for (TableColumn<?, ?> col : tablaParadas.getColumns()) {
            col.setResizable(false);
            col.setReorderable(false);
        }

        tablaParadas.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Parada>() {
            @Override
            public void changed(ObservableValue<? extends Parada> obs, Parada oldVal, Parada newVal) {
                boolean seleccionado = newVal != null;
                btnModificar.setDisable(!seleccionado);
                btnEliminar.setDisable(!seleccionado);
            }
        });

        cmbTipo.valueProperty().addListener(new ChangeListener<TipoParada>() {
            @Override
            public void changed(ObservableValue<? extends TipoParada> obs, TipoParada oldVal, TipoParada newVal) {
                aplicarFiltros();
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

    private void aplicarFiltros() {
        final TipoParada tipoSeleccionado = cmbTipo.getValue();
        final String textoBusqueda = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();

        listaFiltrada.setPredicate(new Predicate<Parada>() {
            @Override
            public boolean test(Parada parada) {
                boolean coincideTipo = (tipoSeleccionado == null) || parada.getTipo() == tipoSeleccionado;
                boolean coincideTexto = textoBusqueda.isEmpty()
                        || parada.getNombreParada().toLowerCase().contains(textoBusqueda)
                        || parada.getCodigo().toLowerCase().contains(textoBusqueda)
                        || parada.getUbicacion().toLowerCase().contains(textoBusqueda);
                return coincideTipo && coincideTexto;
            }
        });

        lblContador.setText(listaFiltrada.size() + " paradas");
    }

    @FXML
    public void handleNuevaParada(ActionEvent e) {
        abrirFormulario(null);
    }

    @FXML
    public void handleModificar(ActionEvent e) {
        Parada seleccionada = tablaParadas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;
        abrirFormulario(seleccionada);
    }

    @FXML
    public void handleEliminar(ActionEvent e) {
        Parada seleccionada = tablaParadas.getSelectionModel().getSelectedItem();
        if (seleccionada == null)
            return;

        GrafoTransporte copia = ServicioGrafo.get().copiar();
        copia.eliminarParada(seleccionada);
        Parada paradaProblema = copia.esConexo();
        if (paradaProblema != null) {
            Alert bloqueo = new Alert(Alert.AlertType.WARNING);
            bloqueo.setTitle("Operación bloqueada");
            bloqueo.setHeaderText(null);
            bloqueo.setContentText("No se puede eliminar \"" + seleccionada.getNombreParada()
                    + "\" porque el grafo quedaría no conexo por la parada \""
                    + paradaProblema.getNombreParada() + "\"");
            bloqueo.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Deseas eliminar la parada \"" + seleccionada.getNombreParada() + "\"?");
        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            paradaCrud.eliminarParada(seleccionada.getCodigo());
            actualizarTabla();
        }
    }

    private void abrirFormulario(Parada parada) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegistroParada.fxml"));
            Parent vista = loader.load();

            RegistroParadaController ctrl = loader.getController();
            ctrl.setParadaCrud(paradaCrud);
            if (parada != null) ctrl.setParada(parada);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(parada != null ? "Modificar Parada" : "Nueva Parada");
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
        Parada seleccionadaAntes = tablaParadas.getSelectionModel().getSelectedItem();
        List<Parada> paradas = paradaCrud.listarParadas();
        listaBase.setAll(paradas);
        aplicarFiltros();

        if (seleccionadaAntes != null) {
            for (Parada p : listaBase) {
                if (p.getCodigo().equals(seleccionadaAntes.getCodigo())) {
                    tablaParadas.getSelectionModel().select(p);
                    break;
                }
            }
        }

        boolean haySeleccion = tablaParadas.getSelectionModel().getSelectedItem() != null;
        btnModificar.setDisable(!haySeleccion);
        btnEliminar.setDisable(!haySeleccion);
    }
}