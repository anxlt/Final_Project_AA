package com.rutas.logico.excepciones;

public class VistaNoCargadaException extends RuntimeException {

    public VistaNoCargadaException(String rutaFxml, Throwable causa) {
        super("No se pudo cargar la vista: " + rutaFxml, causa);
    }
}