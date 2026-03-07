package com.rutas.logico.crud;

import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;

import java.util.ArrayList;
import java.util.List;

public class RutaCrud {

    private final GrafoTransporte grafo;
    private int contador = 0;

    public RutaCrud(GrafoTransporte grafo) {
        this.grafo = grafo;
    }

    public boolean agregarRuta(String nombre, Parada origen, Parada destino,
                               double tiempo, double costo, double distancia, int transbordos) {
        if (grafo.getParadas().size() < 2) return false;
        if (origen.equals(destino)) return false;
        if (existeRuta(origen, destino)) return false;

        contador++;
        Ruta ruta = new Ruta(String.valueOf(contador), nombre, origen, destino);
        ruta.setPeso(Criterio.TIEMPO,      tiempo);
        ruta.setPeso(Criterio.COSTO,       costo);
        ruta.setPeso(Criterio.DISTANCIA,   distancia);
        ruta.setPeso(Criterio.TRANSBORDOS, transbordos);
        grafo.agregarRuta(ruta);
        return true;
    }

    public void modificarRuta(Ruta ruta, String nombre, Parada origen, Parada destino,
                              double tiempo, double costo, double distancia, int transbordos) {

        boolean cambioPares = !ruta.getOrigen().equals(origen) || !ruta.getDestino().equals(destino);

        if (cambioPares) {
            Ruta rutaNueva = new Ruta(ruta.getId(), nombre, origen, destino);
            rutaNueva.setPeso(Criterio.TIEMPO,      tiempo);
            rutaNueva.setPeso(Criterio.COSTO,       costo);
            rutaNueva.setPeso(Criterio.DISTANCIA,   distancia);
            rutaNueva.setPeso(Criterio.TRANSBORDOS, transbordos);
            grafo.modificarRuta(ruta, rutaNueva);
        } else {
            ruta.setNombre(nombre);
            ruta.setPeso(Criterio.TIEMPO,      tiempo);
            ruta.setPeso(Criterio.COSTO,       costo);
            ruta.setPeso(Criterio.DISTANCIA,   distancia);
            ruta.setPeso(Criterio.TRANSBORDOS, transbordos);
        }
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

    private boolean existeRuta(Parada origen, Parada destino) {
        for (Ruta r : grafo.getRutas(origen)) {
            if (r.getDestino().equals(destino)) return true;
        }
        return false;
    }
}