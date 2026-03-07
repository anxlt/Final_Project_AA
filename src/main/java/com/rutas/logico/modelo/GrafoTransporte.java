package com.rutas.logico.modelo;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import java.util.*;

public class GrafoTransporte {

    private final Digraph<Parada, Ruta> grafo = new DigraphEdgeList<>();
    private final Map<String, Vertex<Parada>> verticesPorCodigo = new HashMap<>();

    public void agregarParada(Parada parada) {
        if (verticesPorCodigo.containsKey(parada.getCodigo())) return;
        Vertex<Parada> v = grafo.insertVertex(parada);
        verticesPorCodigo.put(parada.getCodigo(), v);
    }

    public void eliminarParada(Parada parada) {
        Vertex<Parada> v = verticesPorCodigo.remove(parada.getCodigo());
        if (v != null) grafo.removeVertex(v);
    }

    public boolean existeParada(Parada parada) {
        return verticesPorCodigo.containsKey(parada.getCodigo());
    }

    public Parada getParada(Parada clave) {
        Vertex<Parada> v = verticesPorCodigo.get(clave.getCodigo());
        return v != null ? v.element() : null;
    }

    public List<Parada> getParadas() {
        List<Parada> lista = new ArrayList<>();
        for (Vertex<Parada> v : grafo.vertices())
            lista.add(v.element());
        return lista;
    }

    public void modificarParada(Parada parada, String nuevoNombre,
                                String nuevaUbicacion, TipoParada nuevoTipo) {
        Vertex<Parada> v = verticesPorCodigo.get(parada.getCodigo());
        if (v == null) return;
        v.element().setNombreParada(nuevoNombre);
        v.element().setUbicacion(nuevaUbicacion);
        v.element().setTipo(nuevoTipo);
    }


    public void agregarRuta(Ruta ruta) {
        Vertex<Parada> origen  = verticesPorCodigo.get(ruta.getOrigen().getCodigo());
        Vertex<Parada> destino = verticesPorCodigo.get(ruta.getDestino().getCodigo());
        if (origen == null || destino == null) return;
        grafo.insertEdge(origen, destino, ruta);
    }

    public void eliminarRuta(Ruta ruta) {
        Edge<Ruta, Parada> edge = buscarEdge(ruta);
        if (edge != null) grafo.removeEdge(edge);
    }

    public void modificarRuta(Ruta rutaOriginal, Ruta rutaNueva) {
        eliminarRuta(rutaOriginal);
        agregarRuta(rutaNueva);
    }

    public List<Ruta> getRutas(Parada parada) {
        return obtenerVecinos(parada);
    }

    public List<Ruta> obtenerVecinos(Parada parada) {
        Vertex<Parada> v = verticesPorCodigo.get(parada.getCodigo());
        if (v == null) return Collections.emptyList();
        List<Ruta> resultado = new ArrayList<>();
        for (Edge<Ruta, Parada> e : grafo.outboundEdges(v))
            resultado.add(e.element());
        return resultado;
    }


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


    public GrafoTransporte copiar() {
        GrafoTransporte copia = new GrafoTransporte();
        for (Parada p : getParadas()) copia.agregarParada(p);
        for (Edge<Ruta, Parada> e : grafo.edges()) copia.agregarRuta(e.element());
        return copia;
    }


    private Edge<Ruta, Parada> buscarEdge(Ruta ruta) {
        for (Edge<Ruta, Parada> e : grafo.edges())
            if (e.element().getId().equals(ruta.getId())) return e;
        return null;
    }

    public Digraph<Parada, Ruta> getGrafo() {
        return grafo;
    }
}