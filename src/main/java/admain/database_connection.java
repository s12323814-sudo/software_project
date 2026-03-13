package admain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database_connection {

    private static final String URL = "jdbc:postgresql://localhost:5432/Appointment";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456";

    private static Connection connection = null;

    public static Connection getConnection() {

        try {

            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                //System.out.println("Connected to PostgreSQL successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }
}