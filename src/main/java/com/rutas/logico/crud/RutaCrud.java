package com.rutas.logico.crud;

import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;

import java.util.ArrayList;
import java.util.List;

public class RutaCrud {

    private final GrafoTransporte grafo;

    public RutaCrud(GrafoTransporte grafo) {
        this.grafo = grafo;
    }

    public void agregarRuta(String id, Parada origen, Parada destino,
                            double tiempo, double costo, double distancia, int transbordos) {
        Ruta ruta = new Ruta(id, origen, destino);
        ruta.setPeso(Criterio.TIEMPO, tiempo);
        ruta.setPeso(Criterio.COSTO, costo);
        ruta.setPeso(Criterio.DISTANCIA, distancia);
        ruta.setPeso(Criterio.TRANSBORDOS, transbordos);
        grafo.agregarRuta(ruta);
    }

    public void eliminarRuta(Ruta ruta) {
        grafo.eliminarRuta(ruta);
    }

    public List<Ruta> listarRutas() {
        List<Ruta> todas = new ArrayList<>();
        for (Parada parada : grafo.getParadas()) {
            todas.addAll(grafo.getRutas(parada));
        }
        return todas;
    }
}