package com.rutas.logico.database;

import com.rutas.logico.excepciones.DatabaseInitializationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void inicializar() {
        String sqlParadas = """
                CREATE TABLE IF NOT EXISTS paradas (
                    codigo    SERIAL  NOT NULL,
                    nombre    VARCHAR(100) NOT NULL,
                    tipo      VARCHAR(50)  NOT NULL,
                    ubicacion VARCHAR(200),
                    CONSTRAINT paradas_pkey PRIMARY KEY (codigo)
                );
                """;

        String sqlRutas = """
                CREATE TABLE IF NOT EXISTS rutas (
                    id          SERIAL  NOT NULL,
                    nombre      VARCHAR(100) NOT NULL,
                    origen      INTEGER  NOT NULL,
                    destino     INTEGER  NOT NULL,
                    tiempo      DOUBLE PRECISION,
                    costo       DOUBLE PRECISION,
                    distancia   DOUBLE PRECISION,
                    transbordos INTEGER,
                    CONSTRAINT rutas_pkey    PRIMARY KEY (id),
                    CONSTRAINT rutas_origen  FOREIGN KEY (origen)
                        REFERENCES paradas(codigo) ON DELETE CASCADE,
                    CONSTRAINT rutas_destino FOREIGN KEY (destino)
                        REFERENCES paradas(codigo) ON DELETE CASCADE,
                    CONSTRAINT rutas_unica   UNIQUE (origen, destino)
                );
                """;

        try (Connection con = JdbcConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sqlParadas);
            st.execute(sqlRutas);
            System.out.println("Tablas verificadas correctamente.");
        } catch (SQLException e) {
            throw new DatabaseInitializationException(e);
        }
    }
}