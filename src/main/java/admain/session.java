package admain;

public class session {
	  public static Admin currentAdmin = null;

	    public static void logout() {
	        currentAdmin = null;
	        System.out.println("Logged out successfully.");
	    }
}
