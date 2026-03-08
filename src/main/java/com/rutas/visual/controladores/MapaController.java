package com.rutas.visual.controladores;

import com.brunomnsilva.smartgraph.graph.*;
import com.brunomnsilva.smartgraph.graphview.*;
import com.rutas.logico.algoritmos.Dijkstra;
import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;
import com.rutas.servicios.ServicioGrafo;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class MapaController {

    private static final String ESTILO_BASE       = "-fx-fill: #234C6A; -fx-stroke: #456882; -fx-stroke-width: 2;";
    private static final String ESTILO_ORIGEN     = "-fx-fill: #456882; -fx-stroke: white; -fx-stroke-width: 4;";
    private static final String ESTILO_DESTINO    = "-fx-fill: #1B3C53; -fx-stroke: #E3E3E3; -fx-stroke-width: 4;";
    private static final String ESTILO_INTERMEDIO = "-fx-fill: #E3E3E3; -fx-stroke: #234C6A; -fx-stroke-width: 3;";
    private static final String ESTILO_ARISTA_BASE   = "-fx-stroke: #CCCCCC; -fx-stroke-width: 1.5; -fx-fill: transparent;";
    private static final String ESTILO_ARISTA_CAMINO = "-fx-stroke: #F0A500; -fx-stroke-width: 4; -fx-fill: transparent;";

    @FXML private AnchorPane rootPane;
    @FXML private ComboBox<Criterio> cmbCriterio;
    @FXML private Label lblTiempo;
    @FXML private Label lblCosto;
    @FXML private Label lblDistancia;
    @FXML private Label lblTransbordos;
    @FXML private Label lblOrigen;
    @FXML private Label lblDestino;

    private SmartGraphPanel<Parada, Ruta> graphView;
    private Digraph<Parada, Ruta> digraph;

    private Parada paradaOrigen  = null;
    private Parada paradaDestino = null;
    private List<Parada> rutaOptima = new ArrayList<>();

    @FXML
    public void initialize() {
        cmbCriterio.setItems(FXCollections.observableArrayList(Criterio.values()));
        configurarCeldaCriterio();
        cmbCriterio.getSelectionModel().selectFirst();
        cargarGrafo();
    }

    private void configurarCeldaCriterio() {
        Callback<ListView<Criterio>, ListCell<Criterio>> factory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Criterio item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item.name().charAt(0) + item.name().substring(1).toLowerCase());
                if (item != null) setStyle("-fx-text-fill: black;");
            }
        };
        cmbCriterio.setCellFactory(factory);
        cmbCriterio.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Criterio item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item.name().charAt(0) + item.name().substring(1).toLowerCase());
                setStyle("-fx-text-fill: white; -fx-background-color: #1B3C53;");
            }
        });
    }

    public void refrescar() {
        paradaOrigen  = null;
        paradaDestino = null;
        rutaOptima    = new ArrayList<>();
        limpiarResultados();
        actualizarLabels();
        cargarGrafo();
    }

    private void cargarGrafo() {
        if (graphView != null) rootPane.getChildren().remove(graphView);

        digraph = new DigraphEdgeList<>();
        for (Parada p : ServicioGrafo.get().getParadas()) digraph.insertVertex(p);

        for (Parada p : ServicioGrafo.get().getParadas()) {
            for (Ruta r : ServicioGrafo.get().getRutas(p)) {
                Vertex<Parada> origen  = encontrarVertice(r.getOrigen());
                Vertex<Parada> destino = encontrarVertice(r.getDestino());
                if (origen != null && destino != null) {
                    try { digraph.insertEdge(origen, destino, r); } catch (Exception ignored) {}
                }
            }
        }

        SmartGraphProperties props = new SmartGraphProperties(getClass().getResourceAsStream("/smartgraph.properties"));
        graphView = new SmartGraphPanel<>(digraph, props, new SmartCircularSortedPlacementStrategy());
        graphView.getStylesheets().clear();
        graphView.getStylesheets().add(getClass().getResource("/smartgraph.css").toExternalForm());
        graphView.setAutomaticLayout(true);
        graphView.setVertexDoubleClickAction(v -> evaluarSeleccion(v.getUnderlyingVertex().element()));

        AnchorPane.setTopAnchor(graphView, 0.0);
        AnchorPane.setLeftAnchor(graphView, 0.0);
        AnchorPane.setRightAnchor(graphView, 0.0);
        AnchorPane.setBottomAnchor(graphView, 70.0);
        rootPane.getChildren().add(0, graphView);

        Platform.runLater(() -> {
            graphView.init();
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> {
                graphView.update();
                aplicarEstiloBase();
                aplicarEstiloAristasBase();
                registrarTooltipsAristas();
            });
            pause.play();
        });
    }

    private void aplicarEstiloBase() {
        for (Parada p : ServicioGrafo.get().getParadas()) aplicarEstiloVertice(p, ESTILO_BASE);
    }

    private void aplicarEstiloAristasBase() {
        for (Parada p : ServicioGrafo.get().getParadas())
            for (Ruta r : ServicioGrafo.get().getRutas(p)) aplicarEstiloArista(r, ESTILO_ARISTA_BASE);
    }

    private void aplicarEstiloVertice(Parada parada, String estilo) {
        SmartStylableNode stylable = graphView.getStylableVertex(parada);
        if (stylable instanceof Node nodo) {
            nodo.setStyle(estilo);
            if (nodo instanceof javafx.scene.Parent parent)
                for (Node hijo : parent.getChildrenUnmodifiable()) hijo.setStyle(estilo);
        }
    }

    private void aplicarEstiloArista(Ruta ruta, String estilo) {
        SmartStylableNode arista = graphView.getStylableEdge(ruta);
        if (arista instanceof Node nodoArista) aplicarEstiloRecursivo(nodoArista, estilo);
    }

    private void aplicarEstiloRecursivo(Node node, String estilo) {
        if (node instanceof javafx.scene.shape.Shape shape && !(shape instanceof javafx.scene.shape.Circle))
            shape.setStyle(estilo);
        if (node instanceof javafx.scene.Parent parent)
            for (Node hijo : parent.getChildrenUnmodifiable()) aplicarEstiloRecursivo(hijo, estilo);
    }

    private void evaluarSeleccion(Parada parada) {
        if (paradaOrigen != null && paradaOrigen.equals(parada)) {
            aplicarEstiloVertice(parada, ESTILO_BASE);
            paradaOrigen = null;
            lblOrigen.setText("Ninguno");
            limpiarEstilosRuta();
            return;
        }
        if (paradaDestino != null && paradaDestino.equals(parada)) {
            aplicarEstiloVertice(parada, ESTILO_BASE);
            paradaDestino = null;
            lblDestino.setText("Ninguno");
            return;
        }
        if (paradaOrigen == null) {
            paradaOrigen = parada;
            aplicarEstiloVertice(parada, ESTILO_ORIGEN);
            lblOrigen.setText(parada.getNombreParada());
            return;
        }
        if (paradaDestino == null) {
            paradaDestino = parada;
            aplicarEstiloVertice(parada, ESTILO_DESTINO);
            lblDestino.setText(parada.getNombreParada());
            return;
        }
        limpiarSeleccion();
        paradaOrigen = parada;
        aplicarEstiloVertice(parada, ESTILO_ORIGEN);
        lblOrigen.setText(parada.getNombreParada());
    }

    @FXML
    public void handleBuscar(ActionEvent e) {
        Criterio criterio = cmbCriterio.getValue();

        if (paradaOrigen == null || paradaDestino == null || criterio == null) {
            mostrarAlerta("Selección incompleta", "Por favor haz doble click en un origen y un destino en el mapa.");
            return;
        }
        if (paradaOrigen.equals(paradaDestino)) {
            mostrarAlerta("Paradas iguales", "El origen y el destino no pueden ser la misma parada.");
            return;
        }
        if (ServicioGrafo.get().getParadas().size() < 2) {
            mostrarAlerta("Sin paradas", "Debe haber al menos dos paradas para buscar una ruta.");
            return;
        }

        Parada paradaDesconectada = ServicioGrafo.get().esConexo();
        if (paradaDesconectada != null) {
            mostrarAlerta("Grafo no conexo", "La parada \"" + paradaDesconectada.getNombreParada()
                    + "\" no tiene entrada o salida. El grafo no es completamente conexo.");
            return;
        }

        List<Parada> camino = Dijkstra.ejecutar(ServicioGrafo.get(), paradaOrigen, paradaDestino, criterio);

        if (camino == null || camino.isEmpty()) {
            rutaOptima = new ArrayList<>();
            limpiarEstilosRuta();
            limpiarResultados();
            mostrarAlerta("Sin ruta", "No existe ruta entre " + paradaOrigen.getNombreParada()
                    + " y " + paradaDestino.getNombreParada() + " con el criterio seleccionado.");
            return;
        }

        rutaOptima = camino;
        estilizarRutaOptima();

        double totalTiempo = 0, totalCosto = 0, totalDistancia = 0, totalTransbordos = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            for (Ruta ruta : ServicioGrafo.get().obtenerVecinos(camino.get(i))) {
                if (ruta.getDestino().equals(camino.get(i + 1))) {
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

    @FXML
    public void handleLimpiar(ActionEvent e) {
        limpiarSeleccion();
        limpiarResultados();
    }

    private void registrarTooltipsAristas() {
        for (Parada p : ServicioGrafo.get().getParadas()) {
            for (Ruta r : ServicioGrafo.get().getRutas(p)) {
                try {
                    SmartStylableNode edge = graphView.getStylableEdge(r);
                    if (!(edge instanceof Node nodoEdge)) continue;
                    String texto = r.getNombre() + "\n"
                            + "Tiempo: "      + r.getPeso(Criterio.TIEMPO)      + " min\n"
                            + "Costo: $"      + r.getPeso(Criterio.COSTO)       + "\n"
                            + "Distancia: "   + r.getPeso(Criterio.DISTANCIA)   + " km\n"
                            + "Transbordos: " + r.getPeso(Criterio.TRANSBORDOS);
                    Tooltip tooltip = new Tooltip(texto);
                    tooltip.setStyle("-fx-font-size: 11px; -fx-background-color: #1B3C53; -fx-text-fill: white;");
                    Tooltip.install(nodoEdge, tooltip);
                } catch (Exception ignored) {}
            }
        }
    }

    private void estilizarRutaOptima() {
        List<Parada> camino = new ArrayList<>(rutaOptima);
        limpiarEstilosRuta();
        for (int i = 0; i < camino.size() - 1; i++) {
            Parada desde = camino.get(i);
            Parada hacia = camino.get(i + 1);
            if (i > 0) aplicarEstiloVertice(desde, ESTILO_INTERMEDIO);
            for (Ruta r : ServicioGrafo.get().getRutas(desde)) {
                if (r.getDestino().equals(hacia)) {
                    aplicarEstiloArista(r, ESTILO_ARISTA_CAMINO);
                    break;
                }
            }
        }
    }

    private void limpiarEstilosRuta() {
        if (graphView == null) return;
        for (Parada p : ServicioGrafo.get().getParadas()) {
            if (paradaOrigen  != null && paradaOrigen.equals(p)) continue;
            if (paradaDestino != null && paradaDestino.equals(p)) continue;
            aplicarEstiloVertice(p, ESTILO_BASE);
        }
        for (Parada p : ServicioGrafo.get().getParadas())
            for (Ruta r : ServicioGrafo.get().getRutas(p)) aplicarEstiloArista(r, ESTILO_ARISTA_BASE);
        rutaOptima = new ArrayList<>();
    }

    private void limpiarSeleccion() {
        if (paradaOrigen != null)  { aplicarEstiloVertice(paradaOrigen,  ESTILO_BASE); paradaOrigen  = null; }
        if (paradaDestino != null) { aplicarEstiloVertice(paradaDestino, ESTILO_BASE); paradaDestino = null; }
        limpiarEstilosRuta();
        actualizarLabels();
    }

    private Vertex<Parada> encontrarVertice(Parada parada) {
        for (Vertex<Parada> v : digraph.vertices())
            if (v.element().equals(parada)) return v;
        return null;
    }

    private void actualizarLabels() {
        lblOrigen.setText(paradaOrigen   != null ? paradaOrigen.getNombreParada()  : "Ninguno");
        lblDestino.setText(paradaDestino != null ? paradaDestino.getNombreParada() : "Ninguno");
    }

    private void limpiarResultados() {
        lblTiempo.setText("-");
        lblCosto.setText("-");
        lblDistancia.setText("-");
        lblTransbordos.setText("-");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}