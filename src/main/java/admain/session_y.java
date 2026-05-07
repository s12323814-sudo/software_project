package admain;
import java.util.logging.Logger;

public class session_y{
    private static final Logger logger = Logger.getLogger(session_y.class.getName());
    private Account_y account;
    
    public static Account_y currentUser = null;
    public static Account_y currentAdmin = null;

    public session_y(Account_y account) {
        this.account = account;
    }

    public static void logoutUser() {
        if (currentUser != null) {
            logger.info("Logging out user: " + currentUser.getUsername());
            currentUser = null;
        } else {
            logger.warning("No user is currently logged in.");
        }
    }

    public static void logoutAdmin() {
        if (currentAdmin != null) {
            logger.info("Logging out admin: " + currentAdmin.getUsername());
            currentAdmin = null;
        } else {
            logger.warning("No admin is currently logged in.");
        }
    }

    public Account_y getAccount() {
        return account;
    }

    public boolean isAdmin() {
        return account.getRole() == Role_y.ADMIN;
    }

    public boolean isUser() {
        return account.getRole() == Role_y.USER;
    }
}
