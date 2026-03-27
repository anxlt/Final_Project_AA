package com.rutas.logico.database.mapper;

import com.rutas.logico.database.RowMapper;
import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.Ruta;
import com.rutas.logico.modelo.TipoParada;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RutaRowMapper implements RowMapper<Ruta> {

    @Override
    public Ruta get(ResultSet rs) throws SQLException {
        Parada origen  = new Parada(rs.getString("origen"),  "", TipoParada.BUS, "");
        Parada destino = new Parada(rs.getString("destino"), "", TipoParada.BUS, "");

        Ruta ruta = new Ruta(
                rs.getString("id"),
                rs.getString("nombre"),
                origen,
                destino
        );
        ruta.setPeso(Criterio.TIEMPO,      rs.getDouble("tiempo"));
        ruta.setPeso(Criterio.COSTO,       rs.getDouble("costo"));
        ruta.setPeso(Criterio.DISTANCIA,   rs.getDouble("distancia"));
        ruta.setPeso(Criterio.TRANSBORDOS, rs.getInt("transbordos"));
        return ruta;
    }

    @Override
    public String query() {
        return "SELECT id, nombre, origen, destino, tiempo, costo, distancia, transbordos FROM rutas";
    }
}