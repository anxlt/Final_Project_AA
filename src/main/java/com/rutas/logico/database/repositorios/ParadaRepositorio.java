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
    /*
        Nombre: insertar
        Argumentos:
            (Parada) parada: Objeto con los datos de la parada a registrar.
        Objetivo: Persistir una nueva parada en la base de datos y obtener su código autogenerado.
        Retorno: (int) El código único asignado por la base de datos.
     */
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
    /*
        Nombre: actualizar
        Argumentos:
            (Parada) parada: Objeto parada con los datos modificados y su código original.
        Objetivo: Actualizar los campos nombre, tipo y ubicación de una parada existente en la persistencia.
        Retorno: Ninguno.
     */
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
    /*
        Nombre: eliminar
        Argumentos:
            (Parada) parada: Objeto parada que se desea remover.
        Objetivo: Borrar el registro de la parada de la base de datos basándose en su código.
        Retorno: Ninguno.
     */
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
    /*
        Nombre: listarTodas
        Argumentos: Ninguno.
        Objetivo: Recuperar todos los registros de la tabla paradas y convertirlos en una lista de objetos.
        Retorno: (List<Parada>) Lista con todas las paradas encontradas.
     */
    public List<Parada> listarTodas() {
        return service.getAll(new ParadaRowMapper());
    }
}