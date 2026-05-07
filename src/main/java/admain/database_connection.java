package admain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import io.github.cdimascio.dotenv.Dotenv;

public class database_connection {

    private static final Logger logger =
            Logger.getLogger(database_connection.class.getName());

    private static Dotenv dotenv;

    private static Connection connection = null;

  
    public static void init() {
        if (dotenv == null) {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        }
    }

    public static Connection getConnection() {
        try {
            init();

            String url = dotenv.get("DB_URL");
            String user = dotenv.get("DB_USER");
            String password = dotenv.get("DB_PASS");

            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
                logger.info("Connected to DB successfully!");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection error", e);
        }

        return connection;
    }
}