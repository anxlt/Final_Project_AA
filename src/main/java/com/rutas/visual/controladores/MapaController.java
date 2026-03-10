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
import java.util.function.Consumer;

public class MapaController {

    private static final String ESTILO_BASE       = "-fx-fill: #234C6A; -fx-stroke: #456882; -fx-stroke-width: 2;";
    private static final String ESTILO_ORIGEN     = "-fx-fill: #456882; -fx-stroke: white; -fx-stroke-width: 4;";
    private static final String ESTILO_DESTINO    = "-fx-fill: #1B3C53; -fx-stroke: #E3E3E3; -fx-stroke-width: 4;";
    private static final String ESTILO_INTERMEDIO = "-fx-fill: #E3E3E3; -fx-stroke: #234C6A; -fx-stroke-width: 3;";
    private static final String ESTILO_ARISTA_BASE   = "-fx-stroke: #CCCCCC; -fx-stroke-width: 4; -fx-fill: transparent;";
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

      /*
        Nombre: configurarCeldaCriterio
        Argumentos: Ninguno.
        Objetivo: Configurar el ComboBox de criterios para mostrar los valores del enum
                  con la primera letra en mayúscula y el resto en minúscula.
        Retorno: (void) No retorna valor.
     */

    private void configurarCeldaCriterio() {
        Callback<ListView<Criterio>, ListCell<Criterio>> factory = new Callback<ListView<Criterio>, ListCell<Criterio>>() {
            @Override
            public ListCell<Criterio> call(ListView<Criterio> lv) {
                return new ListCell<Criterio>() {
                    @Override
                    protected void updateItem(Criterio item, boolean empty) {
                        super.updateItem(item, empty);
                        setText((item == null || empty) ? null : item.name().charAt(0) + item.name().substring(1).toLowerCase());
                        if (item != null) setStyle("-fx-text-fill: black;");
                    }
                };
            }
        };
        cmbCriterio.setCellFactory(factory);
        cmbCriterio.setButtonCell(new ListCell<Criterio>() {
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

    /*
        Nombre: cargarGrafo
        Argumentos: Ninguno.
        Objetivo: Construir el grafo visual SmartGraph a partir de los datos del ServicioGrafo,
                  configurar el panel con propiedades y estilos, y registrar el evento
                  de doble click para seleccionar paradas.
        Retorno: (void) No retorna valor.
     */

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
        graphView.setVertexDoubleClickAction(new Consumer<SmartGraphVertex<Parada>>() {
            @Override
            public void accept(SmartGraphVertex<Parada> v) {
                evaluarSeleccion(v.getUnderlyingVertex().element());
            }
        });

        AnchorPane.setTopAnchor(graphView, 0.0);
        AnchorPane.setLeftAnchor(graphView, 0.0);
        AnchorPane.setRightAnchor(graphView, 0.0);
        AnchorPane.setBottomAnchor(graphView, 70.0);
        rootPane.getChildren().add(0, graphView);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                graphView.init();
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(new javafx.event.EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        graphView.update();
                        aplicarEstiloBase();
                        aplicarEstiloAristasBase();
                        registrarTooltipsAristas();
                    }
                });
                pause.play();
            }
        });
    }

    /*
        Nombre: aplicarEstiloBase
        Argumentos: Ninguno.
        Objetivo: Restablecer el estilo visual predeterminado en todos los vértices del grafo.
        Retorno: (void) No retorna valor.
     */

    private void aplicarEstiloBase() {
        for (Parada p : ServicioGrafo.get().getParadas()) {
            aplicarEstiloVertice(p, ESTILO_BASE);
        }
    }

    /*
        Nombre: aplicarEstiloAristasBase
        Argumentos: Ninguno.
        Objetivo: Restablecer el estilo visual predeterminado en todas las aristas del grafo.
        Retorno: (void) No retorna valor.
     */

    private void aplicarEstiloAristasBase() {
        for (Parada p : ServicioGrafo.get().getParadas()) {
            for (Ruta r : ServicioGrafo.get().getRutas(p)) {
                aplicarEstiloArista(r, ESTILO_ARISTA_BASE);
            }
        }
    }

    /*
        Nombre: aplicarEstiloVertice
        Argumentos:
            (Parada) parada: Representa la parada cuyo vértice visual se desea estilizar.
            (String) estilo: Representa el string de estilo CSS inline que se aplicará al vértice.
        Objetivo: Aplicar un estilo CSS inline al vértice SmartGraph correspondiente a una parada,
                  incluyendo sus nodos hijos.
        Retorno: (void) No retorna valor.
     */

    private void aplicarEstiloVertice(Parada parada, String estilo) {
        SmartStylableNode stylable = graphView.getStylableVertex(parada);
        if (stylable instanceof Node) {
            Node nodo = (Node) stylable;
            nodo.setStyle(estilo);
            if (nodo instanceof javafx.scene.Parent) {
                javafx.scene.Parent parent = (javafx.scene.Parent) nodo;
                for (Node hijo : parent.getChildrenUnmodifiable()) {
                    hijo.setStyle(estilo);
                }
            }
        }
    }

    /*
        Nombre: aplicarEstiloArista
        Argumentos:
            (Ruta) ruta: Representa la ruta cuya arista visual se desea estilizar.
            (String) estilo: Representa el string de estilo CSS inline que se aplicará a la arista.
        Objetivo: Aplicar un estilo CSS inline a la arista SmartGraph correspondiente a una ruta.
        Retorno: (void) No retorna valor.
     */

    private void aplicarEstiloArista(Ruta ruta, String estilo) {
        SmartStylableNode arista = graphView.getStylableEdge(ruta);
        if (arista instanceof Node) {
            aplicarEstiloRecursivo((Node) arista, estilo);
        }
    }

    /*
        Nombre: aplicarEstiloRecursivo
        Argumentos:
            (Node) node: Representa el nodo JavaFX raíz desde el que se aplicará el estilo.
            (String) estilo: Representa el string de estilo CSS inline que se aplicará.
        Objetivo: Recorrer recursivamente el árbol de nodos JavaFX y aplicar el estilo
                  a todas las formas que no sean círculos (para no afectar los vértices).
        Retorno: (void) No retorna valor.
     */

    private void aplicarEstiloRecursivo(Node node, String estilo) {
        if (node instanceof javafx.scene.shape.Shape && !(node instanceof javafx.scene.shape.Circle)) {
            javafx.scene.shape.Shape shape = (javafx.scene.shape.Shape) node;
            shape.setStyle(estilo);
        }
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (Node hijo : parent.getChildrenUnmodifiable()) {
                aplicarEstiloRecursivo(hijo, estilo);
            }
        }
    }

    /*
        Nombre: evaluarSeleccion
        Argumentos:
            (Parada) parada: Representa la parada sobre la que el usuario hizo doble click.
        Objetivo: Gestionar la selección de origen y destino en el mapa. Si la parada ya es
                  el origen o destino activo, la deselecciona. Si no hay origen, la asigna
                  como tal; si no hay destino, como destino. Si ambos ya están asignados,
                  reinicia la selección y asigna la nueva parada como origen.
        Retorno: (void) No retorna valor.
     */

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

    /*
        Nombre: registrarTooltipsAristas
        Argumentos: Ninguno.
        Objetivo: Registrar tooltips informativos en cada arista del grafo visual,
                  mostrando el nombre de la ruta y sus pesos al pasar el cursor.
        Retorno: (void) No retorna valor.
     */

    private void registrarTooltipsAristas() {
        for (Parada p : ServicioGrafo.get().getParadas()) {
            for (Ruta r : ServicioGrafo.get().getRutas(p)) {
                try {
                    SmartStylableNode edge = graphView.getStylableEdge(r);
                    if (!(edge instanceof javafx.scene.Node)) continue;
                    javafx.scene.Node nodoEdge = (javafx.scene.Node) edge;
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

    /*
        Nombre: estilizarRutaOptima
        Argumentos: Ninguno.
        Objetivo: Resaltar visualmente la ruta óptima calculada, aplicando estilos
                  diferenciados a los vértices intermedios y a las aristas del camino.
        Retorno: (void) No retorna valor.
     */

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

    /*
        Nombre: limpiarEstilosRuta
        Argumentos: Ninguno.
        Objetivo: Restablecer los estilos base de todos los vértices y aristas del grafo,
                  preservando el estilo de los nodos origen y destino actualmente seleccionados.
        Retorno: (void) No retorna valor.
     */

    private void limpiarEstilosRuta() {
        if (graphView == null) return;
        for (Parada p : ServicioGrafo.get().getParadas()) {
            if (paradaOrigen  != null && paradaOrigen.equals(p))  continue;
            if (paradaDestino != null && paradaDestino.equals(p)) continue;
            aplicarEstiloVertice(p, ESTILO_BASE);
        }
        for (Parada p : ServicioGrafo.get().getParadas()) {
            for (Ruta r : ServicioGrafo.get().getRutas(p)) {
                aplicarEstiloArista(r, ESTILO_ARISTA_BASE);
            }
        }
        rutaOptima = new ArrayList<>();
    }

    /*
        Nombre: limpiarSeleccion
        Argumentos: Ninguno.
        Objetivo: Deseleccionar las paradas de origen y destino, restablecer todos los estilos
                  del grafo y limpiar las etiquetas de selección.
        Retorno: (void) No retorna valor.
     */

    private void limpiarSeleccion() {
        if (paradaOrigen  != null) { aplicarEstiloVertice(paradaOrigen,  ESTILO_BASE); paradaOrigen  = null; }
        if (paradaDestino != null) { aplicarEstiloVertice(paradaDestino, ESTILO_BASE); paradaDestino = null; }
        limpiarEstilosRuta();
        actualizarLabels();
    }

    /*
        Nombre: encontrarVertice
        Argumentos:
            (Parada) parada: Representa la parada cuyo vértice se desea localizar en el dígrafo.
        Objetivo: Buscar y retornar el vértice SmartGraph que corresponde a una parada específica.
        Retorno: (Vertex<Parada>) Retorna el vértice encontrado, o null si no existe.
     */

    private Vertex<Parada> encontrarVertice(Parada parada) {
        for (Vertex<Parada> v : digraph.vertices()) {
            if (v.element().equals(parada)) return v;
        }
        return null;
    }

    /*
        Nombre: actualizarLabels
        Argumentos: Ninguno.
        Objetivo: Actualizar las etiquetas de origen y destino en la interfaz según
                  las paradas actualmente seleccionadas.
        Retorno: (void) No retorna valor.
     */

    private void actualizarLabels() {
        lblOrigen.setText(paradaOrigen   != null ? paradaOrigen.getNombreParada()  : "Ninguno");
        lblDestino.setText(paradaDestino != null ? paradaDestino.getNombreParada() : "Ninguno");
    }

    /*
        Nombre: limpiarResultados
        Argumentos: Ninguno.
        Objetivo: Restablecer las etiquetas de resultados (tiempo, costo, distancia, transbordos)
                  a su estado inicial mostrando un guion.
        Retorno: (void) No retorna valor.
     */

    private void limpiarResultados() {
        lblTiempo.setText("-");
        lblCosto.setText("-");
        lblDistancia.setText("-");
        lblTransbordos.setText("-");
    }

    /*
        Nombre: mostrarAlerta
        Argumentos:
            (String) titulo: Representa el título que se mostrará en la ventana de alerta.
            (String) mensaje: Representa el contenido del mensaje de la alerta.
        Objetivo: Mostrar un diálogo de información al usuario con un título y mensaje específicos.
        Retorno: (void) No retorna valor.
     */

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}