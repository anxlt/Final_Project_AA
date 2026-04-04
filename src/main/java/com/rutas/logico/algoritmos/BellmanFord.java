package com.rutas.logico.algoritmos;

import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;

import java.util.*;

public class BellmanFord {

    /*
        Nombre: ejecutar
        Argumentos:
            (GrafoTransporte) grafo: Representa el grafo dirigido sobre el que se realiza la búsqueda.
            (Parada) origen: Representa la parada desde donde inicia el recorrido.
            (Parada) destino: Representa la parada a la que se desea llegar.
            (Criterio) criterio: Representa el peso que se utilizará para calcular la ruta óptima
                                 (tiempo, costo, distancia o transbordos).
        Objetivo: Calcular la ruta óptima entre dos paradas usando el algoritmo de Bellman-Ford
                  según el criterio indicado. A diferencia de Dijkstra, Bellman-Ford relaja todas
                  las aristas del grafo (|V| - 1) veces, lo que le permite manejar pesos negativos.
                  También detecta ciclos negativos y retorna null si los hay.
        Retorno: (List<Parada>) Retorna la lista ordenada de paradas que conforman el camino más
                 corto, null si no existe ruta o si se detecta un ciclo negativo.
     */

    public static List<Parada> ejecutar(GrafoTransporte grafo, Parada origen, Parada destino, Criterio criterio) {

        List<Parada> paradas         = grafo.getParadas();
        Map<Parada, Double> pesos    = new HashMap<>();
        Map<Parada, Parada> anterior = new HashMap<>();

        for (Parada p : paradas) {
            pesos.put(p, Double.MAX_VALUE);
        }
        pesos.put(origen, 0.0);

        int n = paradas.size();
        boolean convergio = false;

        for (int iteracion = 0; iteracion < n - 1 && !convergio; iteracion++) {
            boolean huboActualizacion = false;

            for (Parada actual : paradas) {
                if (pesos.get(actual) != Double.MAX_VALUE) {

                    for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                        Parada vecino  = ruta.getDestino();
                        Object pesoObj = ruta.getPeso(criterio);

                        if (pesoObj != null) {
                            double peso       = ((Number) pesoObj).doubleValue();
                            double nuevoCosto = pesos.get(actual) + peso;

                            if (nuevoCosto < pesos.getOrDefault(vecino, Double.MAX_VALUE)) {
                                pesos.put(vecino, nuevoCosto);
                                anterior.put(vecino, actual);
                                huboActualizacion = true;
                            }
                        }
                    }
                }
            }

            if (!huboActualizacion) {
                convergio = true;
            }
        }

        boolean cicloNegativo = false;

        for (Parada actual : paradas) {
            if (pesos.get(actual) != Double.MAX_VALUE) {

                for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                    Parada vecino  = ruta.getDestino();
                    Object pesoObj = ruta.getPeso(criterio);

                    if (pesoObj != null) {
                        double peso       = ((Number) pesoObj).doubleValue();
                        double nuevoCosto = pesos.get(actual) + peso;

                        if (nuevoCosto < pesos.getOrDefault(vecino, Double.MAX_VALUE)) {
                            cicloNegativo = true;
                        }
                    }
                }
            }
        }

        if (cicloNegativo) return null;

        if (!pesos.containsKey(destino) || pesos.get(destino) == Double.MAX_VALUE) return null;

        List<Parada> camino = new ArrayList<>();
        for (Parada paso = destino; paso != null; paso = anterior.get(paso)) {
            camino.add(paso);
        }

        Collections.reverse(camino);

        if (camino.isEmpty() || !camino.get(0).equals(origen)) return null;

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
                  del grafo y se ejecuta Bellman-Ford, conservando el mejor resultado distinto a todos
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
            boolean encontrada = false;
            Iterator<Ruta> it = grafo.obtenerVecinos(desde).iterator();

            while (it.hasNext() && !encontrada) {
                Ruta r = it.next();
                if (r.getDestino().equals(hasta)) {
                    rutaAEliminar = r;
                    encontrada = true;
                }
            }

            if (rutaAEliminar != null) {
                grafo.eliminarRuta(rutaAEliminar);
                List<Parada> candidato = ejecutar(grafo, origen, destino, criterio);
                grafo.agregarRuta(rutaAEliminar);

                if (candidato != null && !candidato.isEmpty()) {
                    if (esCaminoNuevo(candidato, caminosPrevios)) {
                        double costo = calcularCosto(grafo, candidato, criterio);
                        if (costo < mejorCosto) {
                            mejorCosto = costo;
                            mejorCandidato = candidato;
                        }
                    }
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
            boolean encontrada = false;
            Iterator<Ruta> it = grafo.obtenerVecinos(camino.get(i)).iterator();

            while (it.hasNext() && !encontrada) {
                Ruta r = it.next();
                if (r.getDestino().equals(camino.get(i + 1))) {
                    Object peso = r.getPeso(criterio);
                    if (peso instanceof Number) {
                        total += ((Number) peso).doubleValue();
                    }
                    encontrada = true;
                }
            }
        }
        return total;
    }
}