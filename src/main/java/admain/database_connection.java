package admain;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import io.github.cdimascio.dotenv.Dotenv;

public class Databaseconnection {

    private static final Logger logger = Logger.getLogger(Databaseconnection.class.getName());
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASS");
    private static Connection connection = null;

    private Databaseconnection() {
        /* Utility class should not be instantiated */
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                logger.info("Connected to DB successfully!");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, () -> "Database connection error: " + e.getMessage(), e);
        }
        return connection;
    }
}
