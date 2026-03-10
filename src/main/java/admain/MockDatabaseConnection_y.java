package admain;

import java.sql.Connection;
import java.sql.DriverManager;

public class MockDatabaseConnection_y {
    public static Connection getConnection() throws Exception {

        String url = "jdbc:h2:mem:testdb";
        String user = "sa";
        String password = "";

        return DriverManager.getConnection(url, user, password);
    }

}
