package com.rutas.logico.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnection {

    private static final String URL      = "jdbc:postgresql://localhost:5432/rutas_transporte";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "postgres";

    /*
    Nombre: getConnection
    Argumentos: Ninguno.
    Objetivo: Establecer una conexión activa con el servidor PostgreSQL utilizando las credenciales configuradas (URL, USER, PASSWORD).
    Retorno: (Connection) Retorna el objeto de conexión JDBC para interactuar con la base de datos.
 */

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}