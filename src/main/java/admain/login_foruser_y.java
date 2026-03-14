package admain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

public class login_foruser_y {

    public static users_y login(String username, String password) {

        try (Connection conn = database_connection.getConnection()) {

            String sql = "SELECT * FROM users WHERE username=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String hashedPassword = rs.getString("password");

                if (BCrypt.checkpw(password, hashedPassword)) {
                    return new users_y(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            hashedPassword,
                            rs.getString("email")
                    );
                } else {
                    System.out.println("Wrong password.");
                }
            } else {
                System.out.println("Username not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    
    public static boolean register(String username, String password, String email) {

        try (Connection conn = database_connection.getConnection()) {

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            String sql = "INSERT INTO users(username, password, email) VALUES (?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Username or email already exists.");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePassword(String email, String newPassword) {

        try (Connection conn = database_connection.getConnection()) {

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            String sql = "UPDATE users SET password=? WHERE email=?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Password updated successfully.");
                return true;
            } else {
                System.out.println("Email not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean emailExists(String email) {

        try (Connection conn = database_connection.getConnection()) {

            String sql = "SELECT 1 FROM users WHERE email=?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}