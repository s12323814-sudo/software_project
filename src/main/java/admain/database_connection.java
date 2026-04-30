package admain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database_connection {

    private static final String URL =
        System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:postgresql://localhost:5432/Appointment"
        );

    private static final String USER =
        System.getenv().getOrDefault(
            "DB_USER",
            "postgres"
        );

    private static final String PASSWORD =
        System.getenv().getOrDefault(
            "DB_PASSWORD",
            "123456"
        );

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }

        return connection;
    }
}
