package com.rutas.logico.crud;

import com.rutas.logico.database.repositorios.ParadaRepositorio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.TipoParada;

import java.util.List;

public class ParadaCrud {

    private final GrafoTransporte grafo;
    private final ParadaRepositorio repo = new ParadaRepositorio();

    public ParadaCrud(GrafoTransporte grafo) {
        this.grafo = grafo;
    }

    /*
        Nombre: insertarParada
        Argumentos:
            (String) nombre: Representa el nombre que se asignará a la nueva parada.
            (TipoParada) tipo: Representa el tipo de transporte de la parada.
            (String) ubicacion: Representa la dirección o ubicación de la parada.
        Objetivo: Crear e insertar una nueva parada en el grafo con un código único autogenerado.
        Retorno: (boolean) Retorna true si la parada fue insertada exitosamente, false si los datos son inválidos.
     */

    public boolean insertarParada(String nombre, TipoParada tipo, String ubicacion) {
        if (nombre == null || nombre.isBlank() || tipo == null)
            return false;

        Parada nueva = new Parada(null, nombre, tipo, ubicacion);
        int idGenerado = repo.insertar(nueva);
        nueva.setCodigo(String.valueOf(idGenerado));
        grafo.agregarParada(nueva);
        return true;
    }

    /*
        Nombre: modificarParada
        Argumentos:
            (String) codigo: Representa el identificador de la parada que se desea modificar.
            (String) nuevoNombre: Representa el nuevo nombre de la parada.
            (TipoParada) nuevoTipo: Representa el nuevo tipo de transporte.
            (String) nuevaUbicacion: Representa la nueva ubicación de la parada.
        Objetivo: Actualizar los datos de una parada existente identificada por su código.
        Retorno: (boolean) Retorna true si la modificación fue exitosa, false si la parada no existe.
     */

    public boolean modificarParada(Parada parada, String nuevoNombre, TipoParada nuevoTipo, String nuevaUbicacion) {

        grafo.modificarParada(parada, nuevoNombre, nuevaUbicacion, nuevoTipo);
        repo.actualizar(parada);
        return true;
    }

    /*
        Nombre: eliminarParada
        Argumentos:
            (String) codigo: Representa el identificador de la parada que se desea eliminar.
        Objetivo: Eliminar una parada del grafo junto con todas sus rutas asociadas.
        Retorno: (boolean) Retorna true si la parada fue eliminada, false si no existía.
     */

    public boolean eliminarParada(Parada parada) {

        grafo.eliminarParada(parada);
        repo.eliminar(parada);
        return true;
    }

    /*
        Nombre: listarParadas
        Argumentos: Ninguno.
        Objetivo: Obtener la lista completa de paradas registradas en el grafo.
        Retorno: (List<Parada>) Retorna todas las paradas actualmente almacenadas.
     */

    public List<Parada> listarParadas() {
        return grafo.getParadas();
    }
}