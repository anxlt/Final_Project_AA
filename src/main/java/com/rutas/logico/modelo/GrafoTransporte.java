package com.rutas.logico.modelo;

import java.util.*;

public class GrafoTransporte {

    private final Map<Parada, List<Ruta>> listaAdyacencia;

    public GrafoTransporte() {
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

    public void eliminarRuta(Ruta ruta){
        Parada origen = ruta.getOrigen();
        List<Ruta> rutas = listaAdyacencia.get(origen);
        if(rutas != null)
            rutas.remove(ruta);

    }

    public List<Ruta> getRutas(Parada parada){
        return listaAdyacencia.getOrDefault(parada, new ArrayList<>());
    }

    public void modificarParada(Parada parada, String nuevoNombre, String nuevaUbicacion, TipoParada nuevoTipo) {
        for (Parada p : listaAdyacencia.keySet()) {
            if (p.equals(parada)) {
                p.setNombreParada(nuevoNombre);
                p.setUbicacion(nuevaUbicacion);
                p.setTipo(nuevoTipo);
            }
        }
    }

    public void modificarRuta(Ruta rutaOriginal, Ruta rutaNueva) {

        List<Ruta> rutasDelOrigen = listaAdyacencia.get(rutaOriginal.getOrigen());
        if (rutasDelOrigen == null) return;

        for (int i = 0; i < rutasDelOrigen.size(); i++) {
            if (rutasDelOrigen.get(i).getId().equals(rutaOriginal.getId())) {

                if (rutaOriginal.getOrigen().equals(rutaNueva.getOrigen())) {
                    rutasDelOrigen.set(i, rutaNueva);
                } else {
                    rutasDelOrigen.remove(i);
                    listaAdyacencia.putIfAbsent(rutaNueva.getOrigen(), new ArrayList<>());
                    listaAdyacencia.get(rutaNueva.getOrigen()).add(rutaNueva);
                }

                listaAdyacencia.putIfAbsent(rutaNueva.getDestino(), new ArrayList<>());
            }
        }
    }

    public boolean existeParada(Parada parada) {
        return listaAdyacencia.containsKey(parada);
    }

    public Parada getParada(Parada clave) {
        for (Parada p : listaAdyacencia.keySet())
            if (p.equals(clave)) return p;
        return null;
    }

    public List<Parada> getParadas() {
        return new ArrayList<>(listaAdyacencia.keySet());
    }


    public List<Ruta> obtenerVecinos(Parada parada) {
        if (!listaAdyacencia.containsKey(parada))
            return Collections.emptyList();
        return Collections.unmodifiableList(listaAdyacencia.get(parada));
    }


    public List<Parada> dijkstra(Parada origen, Parada destino, Criterio criterio) {

        Map<Parada, Double> costos = new HashMap<>();
        Map<Parada, Parada> anterior = new HashMap<>();
        PriorityQueue<double[]> cola = new PriorityQueue<>(
                Comparator.comparingDouble(entrada -> entrada[1])
        );
        Set<Parada> visitados = new HashSet<>();
        Map<Integer, Parada> hashAParada = new HashMap<>();

        for (Parada p : listaAdyacencia.keySet()) {
            costos.put(p, Double.MAX_VALUE);
            hashAParada.put(p.hashCode(), p);
        }

        costos.put(origen, 0.0);
        cola.add(new double[]{origen.hashCode(), 0.0});

        while (!cola.isEmpty()) {

            double[] actual = cola.poll();
            Parada paradaActual = hashAParada.get((int) actual[0]);

            if (!visitados.contains(paradaActual)) {
                visitados.add(paradaActual);

                if (!paradaActual.equals(destino)) {
                    for (Ruta ruta : obtenerVecinos(paradaActual)) {

                        Parada vecino = ruta.getDestino();
                        Object pesoObj = ruta.getPeso(criterio);

                        if (!visitados.contains(vecino) && pesoObj != null) {
                            double nuevoCosto = costos.get(paradaActual) + ((Number) pesoObj).doubleValue();

                            if (nuevoCosto < costos.get(vecino)) {
                                costos.put(vecino, nuevoCosto);
                                anterior.put(vecino, paradaActual);
                                cola.add(new double[]{vecino.hashCode(), nuevoCosto});
                            }
                        }
                    }
                }
            }
        }

        if (costos.get(destino) == Double.MAX_VALUE) return null;

        List<Parada> camino = new ArrayList<>();
        Parada paso = destino;

        while (paso != null) {
            camino.add(paso);
            paso = anterior.get(paso);
        }

        Collections.reverse(camino);
        return camino;
    }

}
