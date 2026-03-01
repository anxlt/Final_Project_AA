package com.rutas.logico.modelo;

import java.util.*;

public class GrafoTransporte {

    private final Map<Parada, List<Ruta>> listaAdyacencia;

    public GrafoTransporte(Map<Parada, List<Ruta>> listaAdyacencia) {
        this.listaAdyacencia = new HashMap<>();
    }

    public void agregarParada(Parada parada) {
        if (listaAdyacencia.containsKey(parada))
            return;

        listaAdyacencia.put(parada, new ArrayList<>());
    }

    public void eliminarParada(Parada parada) {
        listaAdyacencia.remove(parada);
        for(List<Ruta> rutas : listaAdyacencia.values())
        {
            rutas.removeIf(r -> r.getDestino().equals(parada));
        }

    }

    public void agregarRuta(Ruta ruta) {
        Parada origen = ruta.getOrigen();
        listaAdyacencia.putIfAbsent(origen, new ArrayList<>());
        listaAdyacencia.get(origen).add(ruta);
    }


}
