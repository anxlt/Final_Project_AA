package com.rutas.logico.modelo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Ruta {

    private String id;
    private String nombre;
    private Parada origen;
    private Parada destino;
    private Map<Criterio, Object> pesos;

    public Ruta(String id, String nombre, Parada origen, Parada destino) {
        this.id = id;
        this.nombre = nombre;
        this.origen = origen;
        this.destino = destino;
        this.pesos = new HashMap<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Parada getOrigen() { return origen; }
    public Parada getDestino() { return destino; }

    public void setPeso(Criterio criterio, Object valor) { pesos.put(criterio, valor); }
    public Object getPeso(Criterio criterio) { return pesos.get(criterio); }
    public Map<Criterio, Object> getTodosLosPesos() { return pesos; }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ruta ruta = (Ruta) o;
        return Objects.equals(id, ruta.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre;
    }
}