package admain;

public enum Role_y {
    USER,
 
    ADMIN;

   
	public static Role_y fromString(String value) {
	    if (value == null) {
	        throw new IllegalArgumentException("Role cannot be null");
	    }

	    String normalized = value.trim().toUpperCase();

	    switch (normalized) {
	       case "USER", "U":
	            return USER;

	        case "ADMIN","A":
	            return ADMIN;

	        default:
	        	throw new IllegalArgumentException(
	        		    "Invalid role: " + value + ". Allowed values: USER or ADMIN"
	        		);
	    }
	}
}

