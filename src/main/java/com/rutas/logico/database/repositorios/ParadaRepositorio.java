package com.rutas.logico.database.repositorios;

import com.rutas.logico.database.PreparedStatementMapper;
import com.rutas.logico.database.StatementService;
import com.rutas.logico.database.mapper.ParadaRowMapper;
import com.rutas.logico.modelo.Parada;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ParadaRepositorio {

    private final StatementService<Parada> service = StatementService.getInstance();

    public int insertar(Parada parada) {
        return service.executeUpdateAndGetId(parada, new PreparedStatementMapper<>() {
            @Override
            public int execute(Parada p, PreparedStatement ps) throws SQLException {
                ps.setString(1, p.getNombreParada());
                ps.setString(2, p.getTipo().name());
                ps.setString(3, p.getUbicacion());
                return ps.executeUpdate();
            }
            @Override
            public String query() {
                return "INSERT INTO paradas (nombre, tipo, ubicacion) VALUES (?, ?, ?)";
            }
        });
    }

    public void actualizar(Parada parada) {
        service.executeUpdate(parada, new PreparedStatementMapper<>() {
            @Override
            public int execute(Parada p, PreparedStatement ps) throws SQLException {
                ps.setString(1, p.getNombreParada());
                ps.setString(2, p.getTipo().name());
                ps.setString(3, p.getUbicacion());
                ps.setInt(4, Integer.parseInt(p.getCodigo()));
                return ps.executeUpdate();
            }
            @Override
            public String query() {
                return "UPDATE paradas SET nombre = ?, tipo = ?, ubicacion = ? WHERE codigo = ?";
            }
        });
    }

    public void eliminar(Parada parada) {
        service.executeUpdate(parada, new PreparedStatementMapper<>() {
            @Override
            public int execute(Parada p, PreparedStatement ps) throws SQLException {
                ps.setInt(1, Integer.parseInt(p.getCodigo()));
                return ps.executeUpdate();
            }
            @Override
            public String query() {
                return "DELETE FROM paradas WHERE codigo = ?";
            }
        });
    }

    public List<Parada> listarTodas() {
        return service.getAll(new ParadaRowMapper());
    }
}