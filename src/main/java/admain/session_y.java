package admain;

import admain.Admin_y;
import admain.users_y;

public class session_y {
	  public static users_y currentUser = null;  
	    public static Admin_y currentAdmin = null;

	    public static void logoutUser() {
	        currentUser = null;
	        System.out.println("User logged out successfully.");
	    }

	    public static void logout() {
	        currentAdmin = null;
	        System.out.println("Admin logged out successfully.");
	    }
}