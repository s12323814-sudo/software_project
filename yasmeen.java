import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class yasmeen {
public static void main(String[] args){
int x;
x=33;
System.out.println("hello"+x);

 }
}



public class DBConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/clinic_db";
    private static final String USER = "postgresql";
    private static final String PASSWORD = "your_password";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Database Connection Failed!");
            e.printStackTrace();
            return null;
        }
    }
}