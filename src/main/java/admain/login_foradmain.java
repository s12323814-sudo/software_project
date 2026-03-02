package admain;
import java.sql.*;
public class login_foradmain {

	    public static Admin login(String username, String password) {

	        try (Connection conn = database_connection.getConnection()) {

	            String sql = "SELECT * FROM admin WHERE username=? AND password=?";
	            PreparedStatement stmt = conn.prepareStatement(sql);
	            stmt.setString(1, username);
	            stmt.setString(2, password);

	            ResultSet rs = stmt.executeQuery();

	            if (rs.next()) {
	                return new Admin(
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

	    public static boolean register(String username, String password) {

	        try (Connection conn = database_connection.getConnection()) {

	            String sql = "INSERT INTO admin(username, password) VALUES (?,?)";
	            PreparedStatement stmt = conn.prepareStatement(sql);
	            stmt.setString(1, username);
	            stmt.setString(2, password);

	            stmt.executeUpdate();
	            return true;

	        } catch (SQLException e) {
	            System.out.println("Username already exists.");
	            return false;
	        }
	    }
	}

