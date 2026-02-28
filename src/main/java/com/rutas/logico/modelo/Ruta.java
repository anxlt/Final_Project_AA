package com.rutas.logico.modelo;

import java.util.HashMap;
import java.util.Map;

public class Ruta {

    private String id;
    private Parada origen;
    private Parada destino;
    private Map<Criterio,Object> pesos;
    public Ruta(String id, Parada origen, Parada destino) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.pesos = new HashMap<>();

    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setPeso(Criterio criterio, Object valor) {
        pesos.put(criterio, valor);
    }
    public Object getPeso(Criterio criterio) {
        return pesos.get(criterio);
    }
    public Parada getOrigen() { return origen; }
    public Parada getDestino() { return destino; }
    public Map<Criterio, Object> getTodosLosPesos() { return pesos; }

}
