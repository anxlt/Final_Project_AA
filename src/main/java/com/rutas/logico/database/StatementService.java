package com.rutas.logico.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class StatementService<T> {

    private static StatementService<?> INSTANCE;

    private StatementService() {}

    @SuppressWarnings("unchecked")
    public static <T> StatementService<T> getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StatementService<>();
        }
        return (StatementService<T>) INSTANCE;
    }
    /*
    Nombre: executeUpdate
    Argumentos:
        (T) object: Entidad a procesar.
        (PreparedStatementMapper<T>) mapper: Lógica para llenar los parámetros del SQL.
    Objetivo: Ejecutar operaciones de escritura (INSERT, UPDATE, DELETE) de forma genérica.
    Retorno: Ninguno.
 */
    public void executeUpdate(T object, PreparedStatementMapper<T> mapper) {
        try (Connection con = JdbcConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(mapper.query())) {
            mapper.execute(object, ps);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /*
        Nombre: getAll
        Argumentos:
            (RowMapper<T>) rowMapper: Lógica para transformar registros SQL en objetos Java.
        Objetivo: Ejecutar una consulta de lectura y devolver una lista de objetos mapeados.
        Retorno: (List<T>) Colección de objetos recuperados.
     */
    public List<T> getAll(RowMapper<T> rowMapper) {
        List<T> lista = new ArrayList<>();
        try (Connection con = JdbcConnection.getConnection();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(rowMapper.query())) {
            while (rs.next()) lista.add(rowMapper.get(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }
    /*
        Nombre: executeUpdateAndGetId
        Argumentos:
            (T) object: Entidad a insertar.
            (PreparedStatementMapper<T>) mapper: Mapeador de la sentencia.
        Objetivo: Ejecutar un INSERT y recuperar inmediatamente la clave primaria (ID) generada por la base de datos.
        Retorno: (int) El ID autogenerado.
     */
    public int executeUpdateAndGetId(T object, PreparedStatementMapper<T> mapper) {
        try (Connection con = JdbcConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(mapper.query(), Statement.RETURN_GENERATED_KEYS)) {
            mapper.execute(object, ps);
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new RuntimeException("No se generó ID");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}