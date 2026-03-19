package admain;

public enum Role_y {
    USER,
    ADMIN;

   
    public static Role_y fromString(String value) {
        try {
            return Role_y.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid role: " + value);
        }
    }
}

/////////////////////////////////