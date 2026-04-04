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
        Parada origen  = new Parada(
                String.valueOf(rs.getInt("origen")),
                rs.getString("origen_nombre"),
                TipoParada.valueOf(rs.getString("origen_tipo")),
                rs.getString("origen_ubicacion")
        );
        Parada destino = new Parada(
                String.valueOf(rs.getInt("destino")),
                rs.getString("destino_nombre"),
                TipoParada.valueOf(rs.getString("destino_tipo")),
                rs.getString("destino_ubicacion")
        );
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
        return "SELECT r.id, r.nombre, r.origen, r.destino, r.tiempo, r.costo, r.distancia, r.transbordos, "
                + "po.nombre AS origen_nombre, po.tipo AS origen_tipo, po.ubicacion AS origen_ubicacion, "
                + "pd.nombre AS destino_nombre, pd.tipo AS destino_tipo, pd.ubicacion AS destino_ubicacion "
                + "FROM rutas r "
                + "JOIN paradas po ON po.codigo = r.origen "
                + "JOIN paradas pd ON pd.codigo = r.destino";
    }
}