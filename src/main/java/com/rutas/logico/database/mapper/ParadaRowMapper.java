package com.rutas.logico.database.mapper;

import com.rutas.logico.database.RowMapper;
import com.rutas.logico.modelo.Parada;
import com.rutas.logico.modelo.TipoParada;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ParadaRowMapper implements RowMapper<Parada> {

    @Override
    public Parada get(ResultSet rs) throws SQLException {
        return new Parada(
                rs.getString("codigo"),
                rs.getString("nombre"),
                TipoParada.valueOf(rs.getString("tipo")),
                rs.getString("ubicacion")
        );
    }

    @Override
    public String query() {
        return "SELECT codigo, nombre, tipo, ubicacion FROM paradas";
    }
}
