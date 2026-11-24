package com.canteen.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton database connection helper encapsulating the JDBC DataSource.
 * Demonstrates the Singleton design pattern in Spring Boot environment.
 */
@Component
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private final DataSource dataSource;

    @Autowired
    public DatabaseConnection(DataSource dataSource) {
        this.dataSource = dataSource;
        synchronized (DatabaseConnection.class) {
            instance = this;
        }
    }

    /**
     * Retrieves the Singleton instance of DatabaseConnection.
     * @return DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        return instance;
    }

    /**
     * Obtains a standard java.sql.Connection from the injected DataSource.
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource has not been initialized in DatabaseConnection singleton.");
        }
        return dataSource.getConnection();
    }

    /**
     * Direct access to the underlying DataSource.
     * @return DataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }
}
