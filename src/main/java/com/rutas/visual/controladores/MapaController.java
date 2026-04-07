package com.rutas.visual.controladores;

import com.brunomnsilva.smartgraph.graph.*;
import com.brunomnsilva.smartgraph.graphview.*;
import com.rutas.logico.algoritmos.BellmanFord;
import com.rutas.logico.algoritmos.Dijkstra;
import com.rutas.logico.algoritmos.FloydWarshall;
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
import javafx.beans.value.ChangeListener;
import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.beans.value.ObservableValue;
import com.brunomnsilva.smartgraph.graphview.SmartLabelProvider;

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
    @FXML private ComboBox<String>   cmbAlgoritmo;
    @FXML private Label lblOrigen;
    @FXML private Label lblDestino;
    @FXML private Label lblTotal;
    @FXML private Label lblUnidad;
    @FXML private Button btnAlternativo;



    private SmartGraphPanel<Parada, Ruta> graphView;
    private Digraph<Parada, Ruta> digraph;

    private Parada paradaOrigen  = null;
    private Parada paradaDestino = null;
    private List<Parada> rutaOptima = new ArrayList<>();
    private List<List<Parada>> caminosPrevios = new ArrayList<>();

    @FXML
    public void initialize() {
        btnAlternativo.setDisable(true);

        cmbCriterio.setItems(FXCollections.observableArrayList(Criterio.values()));
        configurarCeldaCriterio();
        cmbCriterio.getSelectionModel().selectFirst();

        cmbAlgoritmo.setItems(FXCollections.observableArrayList(
                "Dijkstra", "Bellman-Ford", "Floyd-Warshall"
        ));
        cmbAlgoritmo.getSelectionModel().selectFirst();
        cmbAlgoritmo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #1B3C53;");
            }
        });
        cmbAlgoritmo.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item);
                if (item != null) setStyle("-fx-text-fill: black;");
            }
        });

        cmbCriterio.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Criterio>() {
            @Override
            public void changed(ObservableValue<? extends Criterio> obs, Criterio oldVal, Criterio newVal) {
                if (newVal != null && graphView != null && graphView.getScene() != null) {
                    graphView.update();
                }
            }
        });


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
        Callback<ListView<Criterio>, ListCell<Criterio>> factory = lv -> new ListCell<Criterio>() {
            @Override
            protected void updateItem(Criterio item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null
                        : item.name().charAt(0) + item.name().substring(1).toLowerCase());
                if (item != null) setStyle("-fx-text-fill: black;");
            }
        };
        cmbCriterio.setCellFactory(factory);
        cmbCriterio.setButtonCell(new ListCell<Criterio>() {
            @Override
            protected void updateItem(Criterio item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null
                        : item.name().charAt(0) + item.name().substring(1).toLowerCase());
                setStyle("-fx-text-fill: white; -fx-background-color: #1B3C53;");
            }
        });
    }

    public void refrescar() {
        paradaOrigen  = null;
        paradaDestino = null;
        rutaOptima    = new ArrayList<>();
        caminosPrevios = new ArrayList<>();
        if (btnAlternativo != null) btnAlternativo.setDisable(true);
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

        SmartGraphProperties props = new SmartGraphProperties(
                getClass().getResourceAsStream("/smartgraph.properties"));
        graphView = new SmartGraphPanel<>(digraph, props,  new SmartRandomPlacementStrategy());
        graphView.getStylesheets().clear();
        graphView.getStylesheets().add(getClass().getResource("/smartgraph.css").toExternalForm());
        graphView.setAutomaticLayout(true);
        graphView.setEdgeLabelProvider(new SmartLabelProvider<Ruta>() {
            @Override
            public String valueFor(Ruta ruta) {
                Criterio criterio = cmbCriterio.getValue();
                if (criterio == null) return "";
                Object peso = ruta.getPeso(criterio);
                return peso != null ? String.valueOf(peso) : "";
            }
        });
        graphView.setVertexDoubleClickAction(v -> evaluarSeleccion(v.getUnderlyingVertex().element()));

        AnchorPane.setTopAnchor(graphView, 0.0);
        AnchorPane.setLeftAnchor(graphView, 0.0);
        AnchorPane.setRightAnchor(graphView, 0.0);
        AnchorPane.setBottomAnchor(graphView, 70.0);
        rootPane.getChildren().add(0, graphView);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                graphView.setOpacity(0);
                graphView.init();

                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (graphView.getScene() != null) {
                            graphView.update();
                            aplicarEstiloBase();
                            aplicarEstiloAristasBase();

                            FadeTransition fade = new FadeTransition(Duration.millis(300), graphView);
                            fade.setFromValue(0);
                            fade.setToValue(1);
                            fade.play();
                        }
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
        for (Parada p : ServicioGrafo.get().getParadas())
            aplicarEstiloVertice(p, ESTILO_BASE);
    }

    /*
        Nombre: aplicarEstiloAristasBase
        Argumentos: Ninguno.
        Objetivo: Restablecer el estilo visual predeterminado en todas las aristas del grafo.
        Retorno: (void) No retorna valor.
     */
    private void aplicarEstiloAristasBase() {
        for (Parada p : ServicioGrafo.get().getParadas())
            for (Ruta r : ServicioGrafo.get().getRutas(p))
                aplicarEstiloArista(r, ESTILO_ARISTA_BASE);
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
                for (Node hijo : ((javafx.scene.Parent) nodo).getChildrenUnmodifiable())
                    hijo.setStyle(estilo);
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
        if (arista instanceof Node) aplicarEstiloRecursivo((Node) arista, estilo);
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
        if (node instanceof javafx.scene.shape.Shape && !(node instanceof javafx.scene.shape.Circle))
            ((javafx.scene.shape.Shape) node).setStyle(estilo);
        if (node instanceof javafx.scene.Parent)
            for (Node hijo : ((javafx.scene.Parent) node).getChildrenUnmodifiable())
                aplicarEstiloRecursivo(hijo, estilo);
    }

    /*
        Nombre: evaluarSeleccion
        Argumentos:
            (Parada) parada: Representa la parada sobre la que el usuario hizo doble click.
        Objetivo: Gestionar la selección de origen y destino en el mapa.
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
        String algoritmo  = cmbAlgoritmo.getValue();

        if (criterio == null || algoritmo == null) return;

        if (ServicioGrafo.get().getParadas().size() < 2) {
            mostrarAlerta("Sin paradas", "Debe haber al menos dos paradas para ejecutar el algoritmo.");
            return;
        }

        if (paradaOrigen == null || paradaDestino == null) {
            mostrarAlerta("Selección incompleta", "Por favor haz doble click en un origen y un destino en el mapa.");
            return;
        }
        if (paradaOrigen.equals(paradaDestino)) {
            mostrarAlerta("Paradas iguales", "El origen y el destino no pueden ser la misma parada.");
            return;
        }

        Parada paradaDesconectada = ServicioGrafo.get().esConexo();
        if (paradaDesconectada != null) {
            mostrarAlerta("Grafo no conexo", "La parada \"" + paradaDesconectada.getNombreParada()
                    + "\" no tiene entrada o salida. El grafo no es completamente conexo.");
            return;
        }

        if ("Dijkstra".equals(algoritmo) && tieneAristasNegativas(criterio)) {
            mostrarAlerta("Algoritmo incompatible",
                    "El grafo tiene aristas con peso negativo en el criterio seleccionado");
            return;
        }

        List<Parada> camino = null;
        if ("Dijkstra".equals(algoritmo)) {
            camino = Dijkstra.ejecutar(ServicioGrafo.get(), paradaOrigen, paradaDestino, criterio);
        } else if ("Bellman-Ford".equals(algoritmo)) {
            camino = BellmanFord.ejecutar(ServicioGrafo.get(), paradaOrigen, paradaDestino, criterio);
        } else if ("Floyd-Warshall".equals(algoritmo)) {
            camino = FloydWarshall.ejecutar(ServicioGrafo.get(), paradaOrigen, paradaDestino, criterio);
        }

        if (camino == null || camino.isEmpty()) {
            rutaOptima = new ArrayList<>();
            caminosPrevios = new ArrayList<>();
            limpiarEstilosRuta();
            mostrarAlerta("Sin ruta", "No existe ruta entre " + paradaOrigen.getNombreParada()
                    + " y " + paradaDestino.getNombreParada() + " con el criterio seleccionado.");
            return;
        }

        caminosPrevios = new ArrayList<>();
        caminosPrevios.add(camino);
        rutaOptima = camino;
        btnAlternativo.setDisable(true);
        estilizarRutaOptima();
        if(!"Floyd-Warshall".equals(algoritmo)) {
            btnAlternativo.setDisable(false);
        }
    }

    @FXML
    public void handleBuscarAlternativo(ActionEvent e) {
        Criterio criterio = cmbCriterio.getValue();

        if (paradaOrigen == null || paradaDestino == null || criterio == null) {
            mostrarAlerta("Alternativo", "Selecciona origen y destino primero.");
            return;
        }
        if (paradaOrigen.equals(paradaDestino)) {
            mostrarAlerta("Alternativo", "El origen y el destino no pueden ser iguales.");
            return;
        }

        List<Parada> alternativo = null;
        if ("Dijkstra".equals(cmbAlgoritmo.getValue())) {
            alternativo = Dijkstra.ejecutarAlternativo(
                    ServicioGrafo.get(), paradaOrigen, paradaDestino, criterio, caminosPrevios);
        } else if ("Bellman-Ford".equals(cmbAlgoritmo.getValue())) {
            alternativo = BellmanFord.ejecutarAlternativo(
                    ServicioGrafo.get(), paradaOrigen, paradaDestino, criterio, caminosPrevios);
        }

        if ("Floyd-Warshall".equals(cmbAlgoritmo.getValue())) {
            mostrarAlerta("Alternativo", "Algoritmo Incompatible para busqueda de caminos alternativos." +
                    "\n\nPresiona limpiar para reiniciar.");
        } else if (alternativo == null || alternativo.isEmpty()) {
            mostrarAlerta("Alternativo", "No hay más caminos alternativos disponibles.\n"
                    + "Total encontrados: " + caminosPrevios.size()
                    + "\n\nPresiona Limpiar para reiniciar.");
            return;
        }


        caminosPrevios.add(alternativo);
        rutaOptima = alternativo;
        estilizarRutaOptima();
    }

    @FXML
    public void handleLimpiar(ActionEvent e) {
        limpiarSeleccion();
        caminosPrevios = new ArrayList<>();
        btnAlternativo.setDisable(true);
    }

    /*
    Nombre: actualizarEtiquetasAristas
    Argumentos: Ninguno.
    Objetivo: Actualizar las etiquetas visibles de cada arista según el criterio
              actualmente seleccionado en el ComboBox.
    Retorno: (void) No retorna valor.
 */
    private void actualizarEtiquetasAristas() {
        Criterio criterio = cmbCriterio.getValue();
        if (graphView == null || criterio == null) return;

        graphView.setEdgeLabelProvider(new SmartLabelProvider<Ruta>() {
            @Override
            public String valueFor(Ruta ruta) {
                return String.valueOf(ruta.getPeso(criterio));
            }
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                graphView.update();
            }
        });
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
        rutaOptima = camino;
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
        actualizarTotal();
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
        for (Parada p : ServicioGrafo.get().getParadas())
            for (Ruta r : ServicioGrafo.get().getRutas(p))
                aplicarEstiloArista(r, ESTILO_ARISTA_BASE);
        rutaOptima = new ArrayList<>();
        lblTotal.setText("—");
        lblUnidad.setText("");
    }

    private void actualizarTotal() {
        Criterio criterio = cmbCriterio.getValue();
        if (rutaOptima == null || rutaOptima.size() < 2 || criterio == null) {
            lblTotal.setText("—");
            lblUnidad.setText("");
            return;
        }
        double total = 0;
        for (int i = 0; i < rutaOptima.size() - 1; i++) {
            Parada desde = rutaOptima.get(i);
            Parada hacia = rutaOptima.get(i + 1);
            for (Ruta r : ServicioGrafo.get().getRutas(desde)) {
                if (r.getDestino().equals(hacia)) {
                    Object peso = r.getPeso(criterio);
                    if (peso instanceof Number) total += ((Number) peso).doubleValue();
                    break;
                }
            }
        }
        lblTotal.setText(String.format("%.1f", total));

        switch (criterio.name()) {
            case "TIEMPO"    -> lblUnidad.setText("min");
            case "DISTANCIA" -> lblUnidad.setText("km");
            case "COSTO"     -> lblUnidad.setText("$");
            default          -> lblUnidad.setText("");
        }
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
        btnAlternativo.setDisable(true);
    }

    /*
        Nombre: encontrarVertice
        Argumentos:
            (Parada) parada: Representa la parada cuyo vértice se desea localizar en el dígrafo.
        Objetivo: Buscar y retornar el vértice SmartGraph que corresponde a una parada específica.
        Retorno: (Vertex<Parada>) Retorna el vértice encontrado, o null si no existe.
     */
    private Vertex<Parada> encontrarVertice(Parada parada) {
        for (Vertex<Parada> v : digraph.vertices())
            if (v.element().equals(parada)) return v;
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
        Nombre: tieneAristasNegativas
        Argumentos:
            (Criterio) criterio: El criterio de peso a revisar.
        Objetivo: Verificar si alguna arista del grafo tiene peso negativo para el criterio dado.
        Retorno: (boolean) true si existe al menos una arista con peso negativo.
     */
    private boolean tieneAristasNegativas(Criterio criterio) {
        for (Parada p : ServicioGrafo.get().getParadas())
            for (Ruta r : ServicioGrafo.get().getRutas(p)) {
                Object pesoObj = r.getPeso(criterio);
                if (pesoObj instanceof Number && ((Number) pesoObj).doubleValue() < 0) return true;
            }
        return false;
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