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

    public void executeUpdate(T object, PreparedStatementMapper<T> mapper) {
        try (Connection con = JdbcConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(mapper.query())) {
            mapper.execute(object, ps);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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