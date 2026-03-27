package com.rutas.logico.crud;

import com.rutas.logico.database.repositorios.RutaRepositorio;
import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.GrafoTransporte;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;

import java.util.ArrayList;
import java.util.List;

public class RutaCrud {

    private final GrafoTransporte grafo;
    private final RutaRepositorio repo = new RutaRepositorio();
    private int contador = 0;

    public RutaCrud(GrafoTransporte grafo) {
        this.grafo = grafo;
        for (Parada p : grafo.getParadas()) {
            for (Ruta r : grafo.getRutas(p)) {
                try {
                    int num = Integer.parseInt(r.getId());
                    if (num > contador) contador = num;
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    /*
        Nombre: agregarRuta
        Argumentos:
            (String) nombre: Representa el nombre descriptivo de la ruta.
            (Parada) origen: Representa la parada de inicio de la ruta.
            (Parada) destino: Representa la parada de fin de la ruta.
            (double) tiempo: Representa la duración del trayecto en minutos.
            (double) costo: Representa el precio del trayecto.
            (double) distancia: Representa la distancia del trayecto en kilómetros.
            (int) transbordos: Representa la cantidad de transbordos necesarios.
        Objetivo: Crear e insertar una nueva ruta en el grafo con un ID único autogenerado,
                  validando que no exista ya una ruta entre el mismo par de paradas.
        Retorno: (boolean) Retorna true si la ruta fue agregada, false si no cumple las validaciones.
     */

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
        repo.insertar(ruta);
        return true;
    }

    /*
        Nombre: modificarRuta
        Argumentos:
            (Ruta) ruta: Representa la ruta existente que se desea modificar.
            (String) nombre: Representa el nuevo nombre de la ruta.
            (Parada) origen: Representa la nueva parada de origen.
            (Parada) destino: Representa la nueva parada de destino.
            (double) tiempo: Representa el nuevo tiempo del trayecto en minutos.
            (double) costo: Representa el nuevo costo del trayecto.
            (double) distancia: Representa la nueva distancia en kilómetros.
            (int) transbordos: Representa la nueva cantidad de transbordos.
        Objetivo: Actualizar los datos de una ruta existente. Si cambia el par origen-destino,
                  se elimina la arista y se crea una nueva; si no, solo se actualizan los pesos.
        Retorno: (void) No retorna valor.
     */

    public void modificarRuta(Ruta ruta, String nombre, Parada origen, Parada destino,
                              double tiempo, double costo, double distancia, int transbordos) {

        Ruta rutaNueva = new Ruta(ruta.getId(), nombre, origen, destino);
        rutaNueva.setPeso(Criterio.TIEMPO,      tiempo);
        rutaNueva.setPeso(Criterio.COSTO,       costo);
        rutaNueva.setPeso(Criterio.DISTANCIA,   distancia);
        rutaNueva.setPeso(Criterio.TRANSBORDOS, transbordos);
        grafo.modificarRuta(ruta, rutaNueva);
        repo.actualizar(rutaNueva);

    }

    /*
        Nombre: eliminarRuta
        Argumentos:
            (Ruta) ruta: Representa la ruta que se desea eliminar del grafo.
        Objetivo: Eliminar una ruta del grafo dirigido.
        Retorno: (void) No retorna valor.
     */

    public void eliminarRuta(Ruta ruta) {
        grafo.eliminarRuta(ruta);
        repo.eliminar(ruta);
    }

    /*
        Nombre: listarRutas
        Argumentos: Ninguno.
        Objetivo: Obtener la lista completa de todas las rutas registradas en el grafo.
        Retorno: (List<Ruta>) Retorna todas las rutas salientes de todas las paradas del grafo.
     */

    public List<Ruta> listarRutas() {
        List<Ruta> todas = new ArrayList<>();
        for (Parada parada : grafo.getParadas()) {
            todas.addAll(grafo.getRutas(parada));
        }
        return todas;
    }

    /*
        Nombre: existeRuta
        Argumentos:
            (Parada) origen: Representa la parada de inicio de la ruta a verificar.
            (Parada) destino: Representa la parada de fin de la ruta a verificar.
        Objetivo: Verificar si ya existe una ruta dirigida entre dos paradas específicas.
        Retorno: (boolean) Retorna true si ya existe la ruta, false en caso contrario.
     */

    private boolean existeRuta(Parada origen, Parada destino) {
        for (Ruta r : grafo.getRutas(origen)) {
            if (r.getDestino().equals(destino)) return true;
        }
        return false;
    }
}