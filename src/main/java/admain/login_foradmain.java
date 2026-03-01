package admain;
import java.sql.*;
public class login_foradmain {

	 public static boolean createAdmin(String username, String password) {
	        try (Connection con = database_connection.getConnection()) {
	            String query = "INSERT INTO admin(username, password) VALUES (?, ?)";
	            PreparedStatement ps = con.prepareStatement(query);
	            ps.setString(1, username);
	            ps.setString(2, password); 
	            return ps.executeUpdate() > 0;
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }
	 
	 public static int login(String username, String password) {
	        try (Connection con = database_connection.getConnection()) {
	            String query = "SELECT admin_id FROM admin WHERE username=? AND password=?";
	            PreparedStatement ps = con.prepareStatement(query);
	            ps.setString(1, username);
	            ps.setString(2, password);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                return rs.getInt("admin_id");
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return -1;
	    }

	  public static boolean isAdminExists() {
	        try (Connection con = database_connection.getConnection()) {
	            String query = "SELECT COUNT(*) FROM admin";
	            Statement st = con.createStatement();
	            ResultSet rs = st.executeQuery(query);
	            if (rs.next()) {
	                return rs.getInt(1) > 0;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return false;
	    }

}
