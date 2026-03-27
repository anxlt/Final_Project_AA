package com.rutas.logico.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementMapper<T> {
    int execute(T object, PreparedStatement ps) throws SQLException;
    String query();
}