package com.rutas.visual.controladores;

import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;
import com.rutas.servicios.ServicioGrafo;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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
    }
}