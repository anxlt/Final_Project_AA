package com.rutas.logico.algoritmos;

import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;

import java.util.*;

public class Dijkstra {


    /*
        Nombre: ejecutar
        Argumentos:
            (GrafoTransporte) grafo: Representa el grafo dirigido sobre el que se realiza la búsqueda.
            (Parada) origen: Representa la parada desde donde inicia el recorrido.
            (Parada) destino: Representa la parada a la que se desea llegar.
            (Criterio) criterio: Representa el peso que se utilizará para calcular la ruta óptima (tiempo, costo, distancia o transbordos).
        Objetivo: Calcular la ruta óptima entre dos paradas usando el algoritmo de Dijkstra según el criterio indicado.
        Retorno: (List<Parada>) Retorna la lista ordenada de paradas que conforman el camino más corto, o null si no existe ruta.
     */

    public static List<Parada> ejecutar(GrafoTransporte grafo, Parada origen, Parada destino, Criterio criterio) {

        Map<Parada, Double> costos   = new HashMap<>();
        Map<Parada, Parada> anterior = new HashMap<>();
        Set<Parada> visitados        = new HashSet<>();

        for (Parada p : grafo.getParadas()) {
            costos.put(p, Double.MAX_VALUE);
        }

        costos.put(origen, 0.0);

        PriorityQueue<Parada> cola = new PriorityQueue<>(new Comparator<Parada>() {
            @Override
            public int compare(Parada a, Parada b) {
                return Double.compare(costos.get(a), costos.get(b));
            }
        });

        cola.add(origen);

        while (!cola.isEmpty()) {
            Parada actual = cola.poll();
            if(!visitados.contains(actual)) {
                visitados.add(actual);

                if (actual.equals(destino))
                    break;

                for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                    Parada vecino = ruta.getDestino();
                    Object pesoObj = ruta.getPeso(criterio);

                    if (!visitados.contains(vecino) && pesoObj != null) {
                        double nuevoCosto = costos.get(actual) + ((Number) pesoObj).doubleValue();
                        if (nuevoCosto < costos.getOrDefault(vecino, Double.MAX_VALUE)) {
                            costos.put(vecino, nuevoCosto);
                            anterior.put(vecino, actual);
                            cola.remove(vecino);
                            cola.add(vecino);
                        }
                    }
                }
            }
        }

        if (!costos.containsKey(destino) || costos.get(destino) == Double.MAX_VALUE) return null;

        List<Parada> camino = new ArrayList<>();
        for (Parada paso = destino; paso != null; paso = anterior.get(paso))
            camino.add(paso);

        Collections.reverse(camino);
        return camino;
    }

    /*
        Nombre: ejecutarAlternativo
        Argumentos:
            (GrafoTransporte) grafo: Representa el grafo dirigido sobre el que se realiza la búsqueda.
            (Parada) origen: Representa la parada desde donde inicia el recorrido.
            (Parada) destino: Representa la parada a la que se desea llegar.
            (Criterio) criterio: Representa el peso que se utilizará para calcular la ruta.
            (List<List<Parada>>) caminosPrevios: Lista de caminos ya encontrados que deben ser excluidos.
        Objetivo: Calcular un camino alternativo que no sea igual a ninguno de los caminos previos.
                  Para cada arista del último camino encontrado, se elimina temporalmente esa arista
                  del grafo y se ejecuta Dijkstra, conservando el mejor resultado distinto a todos
                  los caminos ya conocidos.
        Retorno: (List<Parada>) Retorna el siguiente mejor camino distinto, o null si no existe ninguno.
     */

    public static List<Parada> ejecutarAlternativo(GrafoTransporte grafo, Parada origen, Parada destino,
                                                   Criterio criterio, List<List<Parada>> caminosPrevios) {
        if (caminosPrevios == null || caminosPrevios.isEmpty()) return null;

        List<Parada> ultimoCamino = caminosPrevios.get(caminosPrevios.size() - 1);

        List<Parada> mejorCandidato = null;
        double mejorCosto = Double.MAX_VALUE;
        
        for (int i = 0; i < ultimoCamino.size() - 1; i++) {
            Parada desde = ultimoCamino.get(i);
            Parada hasta = ultimoCamino.get(i + 1);

            Ruta rutaAEliminar = null;
            for (Ruta r : grafo.obtenerVecinos(desde)) {
                if (r.getDestino().equals(hasta)) {
                    rutaAEliminar = r;
                    break;
                }
            }
            if (rutaAEliminar == null) continue;

            grafo.eliminarRuta(rutaAEliminar);
            List<Parada> candidato = ejecutar(grafo, origen, destino, criterio);
            grafo.agregarRuta(rutaAEliminar);

            if (candidato == null || candidato.isEmpty()) continue;

            if (esCaminoNuevo(candidato, caminosPrevios)) {
                double costo = calcularCosto(grafo, candidato, criterio);
                if (costo < mejorCosto) {
                    mejorCosto = costo;
                    mejorCandidato = candidato;
                }
            }
        }

        return mejorCandidato;
    }

    /*
        Nombre: esCaminoNuevo
        Argumentos:
            (List<Parada>) candidato: Camino a verificar.
            (List<List<Parada>>) caminosPrevios: Lista de caminos ya conocidos.
        Objetivo: Verificar que el camino candidato no sea idéntico
                  a ninguno de los caminos previos.
        Retorno: (boolean) true si el camino es nuevo, false si ya existe.
     */

    private static boolean esCaminoNuevo(List<Parada> candidato, List<List<Parada>> caminosPrevios) {
        for (List<Parada> previo : caminosPrevios) {
            if (previo.size() == candidato.size()) {
                boolean iguales = true;
                for (int j = 0; j < previo.size(); j++) {
                    if (!previo.get(j).equals(candidato.get(j))) {
                        iguales = false;
                        break;
                    }
                }
                if (iguales) return false;
            }
        }
        return true;
    }

    /*
        Nombre: calcularCosto
        Argumentos:
            (GrafoTransporte) grafo: El grafo con las rutas.
            (List<Parada>) camino: El camino cuyo costo total se desea calcular.
            (Criterio) criterio: El criterio de peso a sumar.
        Objetivo: Calcular el costo total de un camino sumando los pesos de cada arista.
        Retorno: (double) El costo total del camino.
     */

    private static double calcularCosto(GrafoTransporte grafo, List<Parada> camino, Criterio criterio) {
        double total = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            for (Ruta r : grafo.obtenerVecinos(camino.get(i))) {
                if (r.getDestino().equals(camino.get(i + 1))) {
                    Object peso = r.getPeso(criterio);
                    if (peso instanceof Number) total += ((Number) peso).doubleValue();
                    break;
                }
            }
        }
        return total;
    }
}