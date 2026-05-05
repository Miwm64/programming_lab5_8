package ru.spb.miwm64.moviemanager.server.io;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseProvider {
    private final DataSource dataSource;

    public DatabaseProvider(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
