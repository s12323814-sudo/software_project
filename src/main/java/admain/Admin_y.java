package admain;

public class Admin_y {
	  private int adminId;
	    private String username;
	    private String password;

	    public Admin_y(int adminId, String username, String password) {
	        this.adminId = adminId;
	        this.username = username;
	        this.password = password;
	    }

	    public int getAdminId() {
	        return adminId;
	    }

	    public String getUsername() {
	        return username;
	    }

	    public String getPassword() {
	        return password;
	    }
}