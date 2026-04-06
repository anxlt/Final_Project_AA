package com.rutas.logico.algoritmos;

import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;

import java.util.*;

public class FloydWarshall {

    /*
        Nombre: ejecutar
        Argumentos:
            (GrafoTransporte) grafo   : El grafo dirigido sobre el que se ejecuta el algoritmo.
            (Parada)          origen  : La parada desde la que parte el camino.
            (Parada)          destino : La parada a la que se quiere llegar.
            (Criterio)        criterio: El peso a minimizar (TIEMPO, COSTO, DISTANCIA o TRANSBORDOS).
        Objetivo: Calcular el camino de menor costo entre origen y destino usando Floyd-Warshall.
                  Construye internamente la matriz de distancias mínimas entre todos los pares
                  de paradas y luego extrae el camino del par solicitado.
        Retorno: (List<Parada>) Lista ordenada de paradas del camino más corto incluyendo origen
                 y destino, o null si no existe ningún camino entre ambas paradas.
     */
    public static List<Parada> ejecutar(GrafoTransporte grafo, Parada origen, Parada destino, Criterio criterio) {

        List<Parada> paradas = new ArrayList<>(grafo.getParadas());

        Map<Parada, Map<Parada, Double>> dist = new HashMap<>();
        Map<Parada, Map<Parada, Parada>> next = new HashMap<>();

        for (Parada i : paradas) {
            dist.put(i, new HashMap<>());
            next.put(i, new HashMap<>());
            for (Parada j : paradas) {
                dist.get(i).put(j, i.equals(j) ? 0.0 : Double.POSITIVE_INFINITY);
                next.get(i).put(j, null);
            }
        }

        for (Parada p : paradas) {
            for (Ruta ruta : grafo.obtenerVecinos(p)) {
                Object pesoObj = ruta.getPeso(criterio);
                if (!(pesoObj instanceof Number))
                    continue;
                double peso = ((Number) pesoObj).doubleValue();
                Parada d = ruta.getDestino();
                if (peso < dist.get(p).get(d)) {
                    dist.get(p).put(d, peso);
                    next.get(p).put(d, d);
                }
            }
        }

        for (Parada k : paradas) {
            for (Parada i : paradas) {
                if (dist.get(i).get(k) == Double.POSITIVE_INFINITY)
                    continue;
                for (Parada j : paradas) {
                    double ik = dist.get(i).get(k);
                    double kj = dist.get(k).get(j);
                    if (kj == Double.POSITIVE_INFINITY)
                        continue;
                    if (ik + kj < dist.get(i).get(j)) {
                        dist.get(i).put(j, ik + kj);
                        next.get(i).put(j, next.get(i).get(k));
                    }
                }
            }
        }

        if (dist.get(origen).get(destino) == Double.POSITIVE_INFINITY)
            return null;

        List<Parada> camino = new ArrayList<>();
        Parada actual = origen;
        camino.add(actual);
        while (!actual.equals(destino)) {
            actual = next.get(actual).get(destino);
            if (actual == null)
                return null;
            camino.add(actual);
        }

        return camino;
    }
}