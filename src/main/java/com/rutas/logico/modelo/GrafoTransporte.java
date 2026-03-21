package com.rutas.logico.modelo;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import java.util.*;

public class GrafoTransporte {

    private final Digraph<Parada, Ruta> grafo = new DigraphEdgeList<>();
    private final Map<String, Vertex<Parada>> verticesPorCodigo = new HashMap<>(); //Cambiar el mapa (no utilizar String)


    /*
        Nombre: agregarParada
        Argumentos:
            (Parada) parada: Representa la parada que se desea insertar en el grafo.
        Objetivo: Insertar una nueva parada como vértice en el grafo, ignorando la operación si ya existe.
        Retorno: (void) No retorna valor.
     */

    public void agregarParada(Parada parada) {
        if (verticesPorCodigo.containsKey(parada.getCodigo())) return;
        Vertex<Parada> v = grafo.insertVertex(parada);
        verticesPorCodigo.put(parada.getCodigo(), v);
    }

    /*
        Nombre: eliminarParada
        Argumentos:
            (Parada) parada: Representa la parada que se desea eliminar del grafo.
        Objetivo: Eliminar una parada y todas sus aristas asociadas del grafo.
        Retorno: (void) No retorna valor.
     */

    public void eliminarParada(Parada parada) {
        Vertex<Parada> v = verticesPorCodigo.remove(parada.getCodigo());
        if (v != null) grafo.removeVertex(v);
    }

    /*
        Nombre: existeParada
        Argumentos:
            (Parada) parada: Representa la parada cuya existencia se desea verificar.
        Objetivo: Verificar si una parada ya se encuentra registrada en el grafo.
        Retorno: (boolean) Retorna true si la parada existe, false en caso contrario.
     */

    public boolean existeParada(Parada parada) {
        return verticesPorCodigo.containsKey(parada.getCodigo());
    }

    /*
        Nombre: getParada
        Argumentos:
            (Parada) clave: Representa una parada con el código usado como clave de búsqueda.
        Objetivo: Obtener la instancia de una parada almacenada en el grafo a partir de su código.
        Retorno: (Parada) Retorna la parada encontrada, o null si no existe.
    */

    public Parada getParada(Parada clave) {
        Vertex<Parada> v = verticesPorCodigo.get(clave.getCodigo());
        return v != null ? v.element() : null;
    }

    /*
        Nombre: getParadas
        Argumentos: Ninguno.
        Objetivo: Obtener la lista de todas las paradas actualmente registradas en el grafo.
        Retorno: (List<Parada>) Retorna una lista con todas las paradas del grafo.
     */

    public List<Parada> getParadas() {
        return grafo.vertices().stream()
                .map(Vertex::element)
                .collect(java.util.stream.Collectors.toList());
    }

    /*
        Nombre: modificarParada
        Argumentos:
            (Parada) parada: Representa la parada que se desea modificar, identificada por su código.
            (String) nuevoNombre: Representa el nuevo nombre que se asignará a la parada.
            (String) nuevaUbicacion: Representa la nueva ubicación que se asignará a la parada.
            (TipoParada) nuevoTipo: Representa el nuevo tipo de transporte de la parada.
        Objetivo: Actualizar los datos de una parada existente en el grafo sin alterar su posición como vértice.
        Retorno: (void) No retorna valor.
     */

    public void modificarParada(Parada parada, String nuevoNombre,
                                String nuevaUbicacion, TipoParada nuevoTipo) {
        Vertex<Parada> v = verticesPorCodigo.get(parada.getCodigo());
        if (v == null) return;
        v.element().setNombreParada(nuevoNombre);
        v.element().setUbicacion(nuevaUbicacion);
        v.element().setTipo(nuevoTipo);
    }

    /*
        Nombre: agregarRuta
        Argumentos:
            (Ruta) ruta: Representa la ruta con su origen, destino y pesos que se desea insertar.
        Objetivo: Insertar una ruta como arista dirigida entre dos paradas existentes en el grafo.
        Retorno: (void) No retorna valor.
     */

    public void agregarRuta(Ruta ruta) {
        Vertex<Parada> origen  = verticesPorCodigo.get(ruta.getOrigen().getCodigo());
        Vertex<Parada> destino = verticesPorCodigo.get(ruta.getDestino().getCodigo());
        if (origen == null || destino == null) return;
        grafo.insertEdge(origen, destino, ruta);
    }

    /*
        Nombre: eliminarRuta
        Argumentos:
            (Ruta) ruta: Representa la ruta que se desea eliminar del grafo.
        Objetivo: Eliminar la arista correspondiente a una ruta del grafo dirigido.
        Retorno: (void) No retorna valor.
     */

    public void eliminarRuta(Ruta ruta) {
        Edge<Ruta, Parada> edge = buscarEdge(ruta);
        if (edge != null) grafo.removeEdge(edge);
    }

    /*
        Nombre: modificarRuta
        Argumentos:
            (Ruta) rutaOriginal: Representa la ruta existente que será reemplazada.
            (Ruta) rutaNueva: Representa la nueva ruta con los datos actualizados.
        Objetivo: Reemplazar una ruta existente por una nueva, eliminando la original e insertando la nueva.
        Retorno: (void) No retorna valor.
     */

    public void modificarRuta(Ruta rutaOriginal, Ruta rutaNueva) {
        eliminarRuta(rutaOriginal);
        agregarRuta(rutaNueva);
    }

    /*
        Nombre: getRutas
        Argumentos:
            (Parada) parada: Representa la parada de la cual se desean obtener las rutas salientes.
        Objetivo: Obtener todas las rutas que parten desde una parada específica.
        Retorno: (List<Ruta>) Retorna la lista de rutas salientes de la parada.
     */

    public List<Ruta> getRutas(Parada parada) {
        return obtenerVecinos(parada);
    }

    /*
        Nombre: obtenerVecinos
        Argumentos:
            (Parada) parada: Representa la parada de la cual se desean obtener los vecinos.
        Objetivo: Obtener todas las rutas salientes de una parada para usarlas en algoritmos de recorrido.
        Retorno: (List<Ruta>) Retorna la lista de rutas dirigidas que salen desde la parada indicada.
     */

    public List<Ruta> obtenerVecinos(Parada parada) {
        Vertex<Parada> v = verticesPorCodigo.get(parada);
        if (v == null) return Collections.emptyList();
        return grafo.outboundEdges(v).stream()
                .map(Edge::element)
                .collect(java.util.stream.Collectors.toList());
    }

    /*
        Nombre: esConexo
        Argumentos: Ninguno.
        Objetivo: Verificar si el grafo es fuertemente conexo, es decir, que desde cualquier parada
                  se puede alcanzar cualquier otra siguiendo las aristas dirigidas. Realiza dos BFS:
                  uno hacia adelante y otro hacia atrás usando el grafo invertido.
        Retorno: (Parada) Retorna null si el grafo es fuertemente conexo, o la primera parada
                 problemática encontrada en caso contrario.
     */

    public Parada esConexo() {
        if (grafo.numVertices() == 0 || grafo.numVertices() == 1) return null;

        Vertex<Parada> inicio = grafo.vertices().iterator().next();

        Set<Parada> visitadosAdelante = new HashSet<>();
        Queue<Parada> cola = new LinkedList<>();
        cola.add(inicio.element());
        visitadosAdelante.add(inicio.element());

        while (!cola.isEmpty()) {
            Parada actual = cola.poll();
            for (Ruta ruta : obtenerVecinos(actual)) {
                Parada vecino = ruta.getDestino();
                if (!visitadosAdelante.contains(vecino)) {
                    visitadosAdelante.add(vecino);
                    cola.add(vecino);
                }
            }
        }

        for (Parada p : getParadas()) {
            if (!visitadosAdelante.contains(p)) return p;
        }

        Map<Parada, List<Parada>> predecesores = new HashMap<>();
        for (Parada p : getParadas()) predecesores.put(p, new ArrayList<>());

        for (Edge<Ruta, Parada> e : grafo.edges()) {
            Parada dest = e.element().getDestino();
            Parada orig = e.element().getOrigen();
            predecesores.get(dest).add(orig);
        }

        Set<Parada> visitadosAtras = new HashSet<>();
        cola.add(inicio.element());
        visitadosAtras.add(inicio.element());

        while (!cola.isEmpty()) {
            Parada actual = cola.poll();
            for (Parada predecesor : predecesores.get(actual)) {
                if (!visitadosAtras.contains(predecesor)) {
                    visitadosAtras.add(predecesor);
                    cola.add(predecesor);
                }
            }
        }

        for (Parada p : getParadas()) {
            if (!visitadosAtras.contains(p)) return p;
        }

        return null;
    }

    /*
        Nombre: copiar
        Argumentos: Ninguno.
        Objetivo: Crear una copia independiente del grafo con los mismos vértices y aristas,
                  permitiendo simular modificaciones sin afectar el grafo original.
        Retorno: (GrafoTransporte) Retorna una nueva instancia del grafo con las mismas paradas y rutas.
     */

    public GrafoTransporte copiar() {
        GrafoTransporte copia = new GrafoTransporte();
        for (Parada p : getParadas()) copia.agregarParada(p);
        for (Edge<Ruta, Parada> e : grafo.edges()) copia.agregarRuta(e.element());
        return copia;
    }

    /*
        Nombre: buscarEdge
        Argumentos:
            (Ruta) ruta: Representa la ruta cuya arista en el grafo se desea encontrar.
        Objetivo: Localizar la arista interna del grafo que corresponde a una ruta por su identificador.
        Retorno: (Edge<Ruta, Parada>) Retorna la arista encontrada, o null si no existe.
     */

    private Edge<Ruta, Parada> buscarEdge(Ruta ruta) {
        for (Edge<Ruta, Parada> e : grafo.edges())
            if (e.element().getId().equals(ruta.getId())) return e;
        return null;
    }

    public Digraph<Parada, Ruta> getGrafo() {
        return grafo;
    }
}