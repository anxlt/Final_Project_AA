package com.rutas.logico.modelo;

import java.util.Objects;

public class Parada {
    private int codigo;
    private String nombreParada;
    private TipoParada tipo;
    private String ubicacion;

    public Parada(int codigo, String nombreParada, TipoParada tipo, String ubicacion) {
        this.codigo = codigo;
        this.nombreParada = nombreParada;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
    }

    public int getCodigo() { return codigo; }
    public String getNombreParada() { return nombreParada; }
    public TipoParada getTipo() { return tipo; }
    public String getUbicacion() { return ubicacion; }

    public void setCodigo(int codigo) { this.codigo = codigo; }
    public void setNombreParada(String nombreParada) { this.nombreParada = nombreParada; }
    public void setTipo(TipoParada tipo) { this.tipo = tipo; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parada parada = (Parada) o;
        return Objects.equals(codigo, parada.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return nombreParada;
    }

}