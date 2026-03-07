package com.rutas.visual.controladores;

import com.rutas.logico.algoritmos.Dijkstra;
import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;
import com.rutas.servicios.ServicioGrafo;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapaController {

    @FXML private Pane canvasPane;
    @FXML private Canvas canvas;
    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;
    @FXML private ComboBox<Criterio> cmbCriterio;
    @FXML private Label lblParadas;
    @FXML private Label lblTiempo;
    @FXML private Label lblCosto;
    @FXML private Label lblDistancia;
    @FXML private Label lblTransbordos;

    private List<Parada> paradas = new ArrayList<>();
    private List<Ruta> rutas = new ArrayList<>();
    private List<Parada> rutaOptima = new ArrayList<>();
    private Map<Parada, double[]> posiciones = new HashMap<>();

    @FXML
    public void initialize() {
        cmbCriterio.setItems(FXCollections.observableArrayList(Criterio.values()));

        cmbCriterio.setCellFactory(new Callback<ListView<Criterio>, ListCell<Criterio>>() {
            @Override
            public ListCell<Criterio> call(ListView<Criterio> lv) {
                return new ListCell<Criterio>() {
                    @Override
                    protected void updateItem(Criterio item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.name().charAt(0) + item.name().substring(1).toLowerCase());
                            setStyle("-fx-text-fill: black;");
                        }
                    }
                };
            }
        });

        cmbCriterio.setButtonCell(new ListCell<Criterio>() {
            @Override
            protected void updateItem(Criterio item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.name().charAt(0) + item.name().substring(1).toLowerCase());
                    setStyle("-fx-text-fill: white; -fx-background-color: #1B3C53;");
                }
            }
        });

        cmbCriterio.getSelectionModel().selectFirst();

        cmbOrigen.setButtonCell(new ListCell<Parada>() {
            @Override
            protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getNombreParada());
                    setStyle("-fx-text-fill: white; -fx-background-color: #234C6A;");
                }
            }
        });

        cmbDestino.setButtonCell(new ListCell<Parada>() {
            @Override
            protected void updateItem(Parada item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getNombreParada());
                    setStyle("-fx-text-fill: white; -fx-background-color: #234C6A;");
                }
            }
        });

        canvasPane.widthProperty().addListener((obs, oldVal, newVal) -> refrescar());
        canvasPane.heightProperty().addListener((obs, oldVal, newVal) -> refrescar());

        refrescar();
    }

    public void refrescar() {
        paradas = ServicioGrafo.get().getParadas();
        rutas = new ArrayList<>();
        for (Parada p : paradas) {
            rutas.addAll(ServicioGrafo.get().getRutas(p));
        }

        cmbOrigen.setItems(FXCollections.observableArrayList(paradas));
        cmbDestino.setItems(FXCollections.observableArrayList(paradas));

    }

    @FXML
    public void handleBuscar(ActionEvent e) {
        Parada origen = cmbOrigen.getValue();
        Parada destino = cmbDestino.getValue();
        Criterio criterio = cmbCriterio.getValue();

        if (origen == null || destino == null || criterio == null) {
            mostrarAlerta("Selección incompleta", "Por favor selecciona origen, destino y criterio.");
            return;
        }

        if (origen.equals(destino)) {
            mostrarAlerta("Paradas iguales", "El origen y el destino no pueden ser la misma parada.");
            return;
        }

        if (paradas.size() < 2) {
            mostrarAlerta("Sin paradas", "Debe haber al menos dos paradas para buscar una ruta.");
            return;
        }

        List<Parada> camino = Dijkstra.ejecutar(ServicioGrafo.get(), origen, destino, criterio);

        if (camino == null || camino.isEmpty()) {
            rutaOptima = new ArrayList<>();
            dibujar();
            lblParadas.setText("Sin ruta disponible");
            lblTiempo.setText("-");
            lblCosto.setText("-");
            lblDistancia.setText("-");
            lblTransbordos.setText("-");
            mostrarAlerta("Sin ruta", "No existe ruta entre " + origen.getNombreParada()
                    + " y " + destino.getNombreParada() + " con el criterio seleccionado.");
            return;
        }

        rutaOptima = camino;
        dibujar();

        StringBuilder sbRuta = new StringBuilder();
        for (int i = 0; i < camino.size(); i++) {
            if (i > 0) sbRuta.append(" → ");
            sbRuta.append(camino.get(i).getNombreParada());
        }
        lblParadas.setText(sbRuta.toString());

        double totalTiempo = 0, totalCosto = 0, totalDistancia = 0, totalTransbordos = 0;

        for (int i = 0; i < camino.size() - 1; i++) {
            Parada desde = camino.get(i);
            Parada hacia = camino.get(i + 1);

            for (Ruta ruta : ServicioGrafo.get().obtenerVecinos(desde)) {
                if (ruta.getDestino().equals(hacia)) {
                    Object pt  = ruta.getPeso(Criterio.TIEMPO);
                    Object pc  = ruta.getPeso(Criterio.COSTO);
                    Object pd  = ruta.getPeso(Criterio.DISTANCIA);
                    Object ptr = ruta.getPeso(Criterio.TRANSBORDOS);

                    if (pt  instanceof Number) totalTiempo      += ((Number) pt).doubleValue();
                    if (pc  instanceof Number) totalCosto       += ((Number) pc).doubleValue();
                    if (pd  instanceof Number) totalDistancia   += ((Number) pd).doubleValue();
                    if (ptr instanceof Number) totalTransbordos += ((Number) ptr).doubleValue();
                    break;
                }
            }
        }

        lblTiempo.setText(String.format("%.1f min", totalTiempo));
        lblCosto.setText(String.format("$%.2f", totalCosto));
        lblDistancia.setText(String.format("%.1f km", totalDistancia));
        lblTransbordos.setText(String.valueOf((int) totalTransbordos));
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}