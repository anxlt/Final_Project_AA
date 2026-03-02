package com.rutas.servicios;

import com.rutas.logico.modelo.GrafoTransporte;

public class ServicioGrafo {
    private static GrafoTransporte grafo;
    public static void init(GrafoTransporte g) { grafo = g; }
    public static GrafoTransporte get() { return grafo; }
}