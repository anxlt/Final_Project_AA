package com.rutas.logico.crud;

import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.TipoParada;

import java.util.List;

public class ParadaCrud {

    private final GrafoTransporte grafo;

    public ParadaCrud(GrafoTransporte grafo) {
        this.grafo = grafo;
    }

    public boolean insertarParada(String codigo, String nombre, TipoParada tipo, String ubicacion) {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank() || tipo == null)
            return false;

        Parada nueva = new Parada(codigo, nombre, tipo, ubicacion);
        if (grafo.existeParada(nueva))
            return false;

        grafo.agregarParada(nueva);
        return true;
    }

    public boolean modificarParada(String codigo, String nuevoNombre, TipoParada nuevoTipo, String nuevaUbicacion) {
        Parada parada = grafo.getParada(new Parada(codigo, null, null, null));
        if (parada == null)
            return false;

        if (nuevoNombre != null && !nuevoNombre.isBlank())       parada.setNombreParada(nuevoNombre);
        if (nuevoTipo != null)                                   parada.setTipo(nuevoTipo);
        if (nuevaUbicacion != null && !nuevaUbicacion.isBlank()) parada.setUbicacion(nuevaUbicacion);

        return true;
    }

    public boolean eliminarParada(String codigo) {
        Parada clave = new Parada(codigo, null, null, null);
        if (!grafo.existeParada(clave))
            return false;

        grafo.eliminarParada(clave);
        return true;
    }

    public List<Parada> listarParadas() {
        return grafo.getParadas();
    }
}