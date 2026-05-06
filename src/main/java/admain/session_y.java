package admain;

import java.util.logging.Logger;

public class SessionY {

    private static final Logger logger = Logger.getLogger(SessionY .class.getName());

    private Account_y account;

    public static final Account_y currentUser = null;
public static final Account_y currentAdmin = null;

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

    public session_y(Account_y account) {
        this.account = account;
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
