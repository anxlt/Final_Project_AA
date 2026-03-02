package com.rutas.logico.modelo;

public enum TipoParada {
    BUS("Bus"),
    TREN("Tren"),
    TAXI("Taxi");

    private final String nombre;

    TipoParada(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}