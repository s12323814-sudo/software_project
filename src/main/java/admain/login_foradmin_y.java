package admain;
import java.sql.*;

import org.mindrot.jbcrypt.BCrypt;

import admain.Admin_y;
public class login_foradmin_y {

	    public static Admin_y login(String username, String password) {

	        try (Connection conn = database_connection.getConnection()) {

	            String sql = "SELECT * FROM admin WHERE username=? AND password=?";
	            PreparedStatement stmt = conn.prepareStatement(sql);
	            stmt.setString(1, username);
	            stmt.setString(2, password);

	            ResultSet rs = stmt.executeQuery();

	            if (rs.next()) {
	                return new Admin_y(
	                        rs.getInt("admin_id"),
	                        rs.getString("username"),
	                        rs.getString("password")
	                );
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return null;
	    }

	    public static Admin_y register(String username, String password, String email) {

	        try (Connection conn = database_connection.getConnection()) {

	            
	            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

	            String sql = "INSERT INTO admin(username, password, email) VALUES (?,?,?)";
	            PreparedStatement stmt = conn.prepareStatement(sql);

	            stmt.setString(1, username);
	            stmt.setString(2, hashedPassword);
	            stmt.setString(3, email);

	            stmt.executeUpdate();

	          
	            return login(username, password);

	        } catch (SQLException e) {
	            System.out.println("Username already exists.");
	        }

	        return null;
	    }
	    public static boolean updatePassword(String email, String newPassword) {

	        try (Connection conn = database_connection.getConnection()) {

	            String sql = "UPDATE admin SET password=? WHERE email=?";
	            PreparedStatement stmt = conn.prepareStatement(sql);

	            stmt.setString(1, newPassword);
	            stmt.setString(2, email);

	            int rows = stmt.executeUpdate();

	            if(rows > 0){
	                System.out.println("Password updated successfully.");
	                return true;
	            }else{
	                System.out.println("Email not found.");
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return false;
	    }
	    public static boolean emailExists(String email) {

	        try (Connection conn = database_connection.getConnection()) {

	            String sql = "SELECT * FROM admin WHERE email=?";
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

