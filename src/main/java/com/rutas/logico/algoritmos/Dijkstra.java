package com.rutas.logico.algoritmos;

import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;

import java.util.*;

public class Dijkstra {

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

            if (visitados.contains(actual)) continue;
            visitados.add(actual);

            if (actual.equals(destino)) break;

            for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                Parada vecino  = ruta.getDestino();
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

        if (!costos.containsKey(destino) || costos.get(destino) == Double.MAX_VALUE) return null;

        List<Parada> camino = new ArrayList<>();
        for (Parada paso = destino; paso != null; paso = anterior.get(paso))
            camino.add(paso);

        Collections.reverse(camino);
        return camino;
    }
}