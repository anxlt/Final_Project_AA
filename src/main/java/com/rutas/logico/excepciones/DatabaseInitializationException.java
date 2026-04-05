package com.rutas.logico.excepciones;

public class DatabaseInitializationException extends RuntimeException {
    public DatabaseInitializationException( Throwable causa) {
        super("Error al inicializar tablas: ", causa);
    }
}