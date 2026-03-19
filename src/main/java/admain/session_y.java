package admain;

public class session_y {

    private Account_y account;

    public static Account_y currentUser = null;
    public static Account_y currentAdmin = null;

 
    public static void logoutUser() {
        if (currentUser != null) {
            System.out.println("Logging out user: " + currentUser.getUsername());
            currentUser = null;
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

  
    public static void logoutAdmin() {
        if (currentAdmin != null) {
            System.out.println("Logging out admin: " + currentAdmin.getUsername());
            currentAdmin = null;
        } else {
            System.out.println("No admin is currently logged in.");
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
////////////////////////////////