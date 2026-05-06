package admain;


import org.mindrot.jbcrypt.BCrypt;


public class authServicey {

    private AccountRepository_y repo ;
   
  
    public authService_y(AccountRepository_y repo) {
        this.repo = repo;
    }

  
    public authService_y() {
        this.repo = new AccountRepository_y();
    }
    // ================= LOGIN =================
    public session_y login(String input, String password) {
      
        if (input == null || input.isBlank()) {
            System.out.println("Username or Email required");
            return null;
        }
        if (password == null || password.isBlank()) {
            System.out.println("Password required");
            return null;
        }

        Account_y acc = repo.findByUsernameOrEmail(input);
        if (acc == null) {
            System.out.println("Invalid credentials");
            return null;
        }

        String hash = acc.getPasswordHash();
        if (hash == null) {
            throw new RuntimeException("Password hash is null");
        }

        if (!org.mindrot.jbcrypt.BCrypt.checkpw(password, hash)) {
            System.out.println("Invalid credentials");
            return null;
        }

        return new session_y(acc);
    }
    // ================= REGISTER =================
    public Account_y register(String username, String password, String email, Role_y role) {

        // Validation
        if (username == null || username.length() < 3) {
            System.out.println("Username too short");
            return null;
        }

        if (password == null || password.length() < 6) {
            System.out.println("Weak password");
            return null;
        }

        if (email == null || !email.contains("@")) {
            System.out.println("Invalid email");
            return null;
        }

        if (repo.usernameExists(username)) {
            System.out.println("Username already exists!");
            return null;
        }

        if (repo.emailExists(email)) {
            System.out.println("Email already exists!");
            return null;
        }

        // 🔐 Hash password
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        return repo.save(username, hashed, email, role);
    }

    // ================= FORGOT PASSWORD =================
    public boolean updatePassword(String email, String newPassword) {

        if (newPassword == null || newPassword.length() < 6) {
            System.out.println("Weak password");
            return false;
        }

        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        return repo.updatePassword(email, hashed);
    }
    public boolean emailExists(String email) {
        return repo.findByEmail(email) != null;
    }
    // ================= GET ACCOUNT =================
    public Account_y getAccountByEmail(String email) {
        return repo.findByEmail(email);
    }
}
