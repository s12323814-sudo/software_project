package admain;

public class session {
	  public static users currentUser = null;  
	    public static Admin currentAdmin = null;

	    public static void logoutUser() {
	        currentUser = null;
	        System.out.println("User logged out successfully.");
	    }

	    public static void logout() {
	        currentAdmin = null;
	        System.out.println("Admin logged out successfully.");
	    }
}
