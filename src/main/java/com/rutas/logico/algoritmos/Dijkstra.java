package com.rutas.logico.algoritmos;

import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;

import java.util.*;

public class Dijkstra {

    public static List<Parada> ejecutar(GrafoTransporte grafo, Parada origen, Parada destino, Criterio criterio) {

        Map<Parada, Double>     costos      = new HashMap<>();
        Map<Parada, Parada>     anterior    = new HashMap<>();
        PriorityQueue<double[]> cola        = new PriorityQueue<>(Comparator.comparingDouble(e -> e[1]));
        Set<Parada>             visitados   = new HashSet<>();
        Map<Integer, Parada>    hashAParada = new HashMap<>();

        for (Parada p : grafo.getParadas()) {
            costos.put(p, Double.MAX_VALUE);
            hashAParada.put(p.hashCode(), p);
        }

        costos.put(origen, 0.0);
        cola.add(new double[]{ origen.hashCode(), 0.0 });

        boolean destinoAlcanzado = false;

        while (!cola.isEmpty() && !destinoAlcanzado) {
            double[] actual       = cola.poll();
            Parada   paradaActual = hashAParada.get((int) actual[0]);

            if (paradaActual != null && !visitados.contains(paradaActual)) {
                visitados.add(paradaActual);

                if (paradaActual.equals(destino)) {
                    destinoAlcanzado = true;
                } else {
                    for (Ruta ruta : grafo.obtenerVecinos(paradaActual)) {
                        Parada vecino  = ruta.getDestino();
                        Object pesoObj = ruta.getPeso(criterio);

                        if (!visitados.contains(vecino) && pesoObj != null) {
                            double nuevoCosto = costos.get(paradaActual) + ((Number) pesoObj).doubleValue();
                            if (nuevoCosto < costos.getOrDefault(vecino, Double.MAX_VALUE)) {
                                costos.put(vecino, nuevoCosto);
                                anterior.put(vecino, paradaActual);
                                cola.add(new double[]{ vecino.hashCode(), nuevoCosto });
                            }
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
}