package com.rutas.logico.database.repositorios;

import com.rutas.logico.database.PreparedStatementMapper;
import com.rutas.logico.database.StatementService;
import com.rutas.logico.database.mapper.RutaRowMapper;
import com.rutas.logico.modelo.Criterio;
import com.rutas.logico.modelo.Ruta;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class RutaRepositorio {

    private final StatementService<Ruta> service = StatementService.getInstance();

    public void insertar(Ruta ruta) {
        service.executeUpdate(ruta, new PreparedStatementMapper<>() {
            @Override
            public int execute(Ruta r, PreparedStatement ps) throws SQLException {
                ps.setString(1, r.getId());
                ps.setString(2, r.getNombre());
                ps.setString(3, r.getOrigen().getCodigo());
                ps.setString(4, r.getDestino().getCodigo());
                ps.setDouble(5, toDouble(r.getPeso(Criterio.TIEMPO)));
                ps.setDouble(6, toDouble(r.getPeso(Criterio.COSTO)));
                ps.setDouble(7, toDouble(r.getPeso(Criterio.DISTANCIA)));
                ps.setInt   (8, toInt(r.getPeso(Criterio.TRANSBORDOS)));
                return ps.executeUpdate();
            }
            @Override
            public String query() {
                return """
                       INSERT INTO rutas (id, nombre, origen, destino, tiempo, costo, distancia, transbordos)
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                       """;
            }
        });
    }

    public void actualizar(Ruta ruta) {
        service.executeUpdate(ruta, new PreparedStatementMapper<>() {
            @Override
            public int execute(Ruta r, PreparedStatement ps) throws SQLException {
                ps.setString(1, r.getNombre());
                ps.setString(2, r.getOrigen().getCodigo());
                ps.setString(3, r.getDestino().getCodigo());
                ps.setDouble(4, toDouble(r.getPeso(Criterio.TIEMPO)));
                ps.setDouble(5, toDouble(r.getPeso(Criterio.COSTO)));
                ps.setDouble(6, toDouble(r.getPeso(Criterio.DISTANCIA)));
                ps.setInt   (7, toInt(r.getPeso(Criterio.TRANSBORDOS)));
                ps.setString(8, r.getId());
                return ps.executeUpdate();
            }
            @Override
            public String query() {
                return """
                       UPDATE rutas
                       SET nombre=?, origen=?, destino=?, tiempo=?, costo=?, distancia=?, transbordos=?
                       WHERE id=?
                       """;
            }
        });
    }

    public void eliminar(Ruta ruta) {
        service.executeUpdate(ruta, new PreparedStatementMapper<>() {
            @Override
            public int execute(Ruta r, PreparedStatement ps) throws SQLException {
                ps.setString(1, r.getId());
                return ps.executeUpdate();
            }
            @Override
            public String query() {
                return "DELETE FROM rutas WHERE id = ?";
            }
        });
    }

    public List<Ruta> listarTodas() {
        return service.getAll(new RutaRowMapper());
    }

    private static double toDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private static int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        return 0;
    }
}