package com.rutas.logico.crud;

import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.TipoParada;

import java.util.List;

public class ParadaCrud {

    private final GrafoTransporte grafo;
    private int contador = 0;

    public ParadaCrud(GrafoTransporte grafo) {
        this.grafo = grafo;
        for (Parada p : grafo.getParadas()) {
            try {
                int num = Integer.parseInt(p.getCodigo());
                if (num > contador) contador = num;
            } catch (NumberFormatException ignored) {}
        }
    }

    public boolean insertarParada(String nombre, TipoParada tipo, String ubicacion) {
        if (nombre == null || nombre.isBlank() || tipo == null)
            return false;

        contador++;
        String codigo = String.valueOf(contador);
        Parada nueva = new Parada(codigo, nombre, tipo, ubicacion);
        grafo.agregarParada(nueva);
        return true;
    }

    public boolean modificarParada(String codigo, String nuevoNombre, TipoParada nuevoTipo, String nuevaUbicacion) {
        Parada parada = grafo.getParada(new Parada(codigo, null, null, null));
        if (parada == null) return false;

        grafo.modificarParada(parada, nuevoNombre, nuevaUbicacion, nuevoTipo);

        return true;
    }

    public boolean eliminarParada(String codigo) {
        Parada clave = new Parada(codigo, null, null, null);
        if (!grafo.existeParada(clave)) return false;

        grafo.eliminarParada(clave);
        return true;
    }

    public List<Parada> listarParadas() {
        return grafo.getParadas();
    }
}