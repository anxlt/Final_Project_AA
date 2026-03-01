package com.rutas.logico.modelo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GrafoTransporte {

    private final Map<Parada, List<Ruta>> listaAdyacencia;

    public GrafoTransporte(Map<Parada, List<Ruta>> listaAdyacencia) {
        this.listaAdyacencia = new LinkedHashMap<>();
    }

    public void agregarParada(Parada parada) {
        if (listaAdyacencia.containsKey(parada))
            return;

        listaAdyacencia.put(parada, new ArrayList<>());
    }
}
