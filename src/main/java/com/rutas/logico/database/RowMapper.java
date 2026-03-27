package com.rutas.logico.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    T get(ResultSet rs) throws SQLException;
    String query();
}