package admain;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class login_foradmin_y {

  
    public static Admin_y login(String username, String password) {

        try (Connection conn = database_connection.getConnection()) {

            String sql = "SELECT * FROM admin WHERE username=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String hashedPassword = rs.getString("password");

                if (BCrypt.checkpw(password, hashedPassword)) {
                    return new Admin_y(
                            rs.getInt("admin_id"),
                            rs.getString("username"),
                            hashedPassword
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

 
    public static Admin_y register(String username, String password, String email) {

        try (Connection conn = database_connection.getConnection()) {

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            String sql = "INSERT INTO admin(username, password, email) VALUES (?,?,?) RETURNING admin_id";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("admin_id");

                return new Admin_y(
                        id,
                        username,
                        hashedPassword
                );
            }

        } catch (Exception e) {
            System.out.println("Account creation failed.");
            e.printStackTrace();
        }

        return null;
    }
    public static boolean updatePassword(String email, String newPassword) {

        try (Connection conn = database_connection.getConnection()) {

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            String sql = "UPDATE admin SET password=? WHERE email=?";
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

    
    public static boolean usernameExists(String username) {

        try (Connection conn = database_connection.getConnection()) {

            String sql = "SELECT 1 FROM admin WHERE username=?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    
    public static boolean emailExists(String email) {

        try (Connection conn = database_connection.getConnection()) {

            String sql = "SELECT 1 FROM admin WHERE email=?";
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