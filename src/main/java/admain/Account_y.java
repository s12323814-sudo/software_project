package admain;

import java.util.Objects;

////////////////////////////////////////////
/**
 * 
 */
public class Account_y {

    private final int accountId;
    private String username;
    private String passwordHash;
    private String email;
    private Role_y role;

   
    public Account_y(int accountId, String username, String passwordHash, String email, Role_y role) {
        this.accountId = accountId;
        setUsername(username);
        setPasswordHash(passwordHash);
        setEmail(email);
        this.role = role;
    }

  
    public int getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public Role_y getRole() {
        return role;
    }

   
    public void setUsername(String username) {
        if (username == null || username.length() < 3) {
            throw new IllegalArgumentException("Invalid username");
        }
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("Invalid password hash");
        }
        this.passwordHash = passwordHash;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        this.email = email;
    }

    public void setRole(Role_y role) {
        this.role = role;
    }

    
    public boolean isAdmin() {
        return role != null && role == Role_y.ADMIN;
    }

    public boolean isUser() {
        return role != null && role == Role_y.USER;
    }
 
    @Override
    public String toString() {
        return "Account{" +
                "id=" + accountId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account_y)) return false;
        Account_y account = (Account_y) o;
        return accountId == account.accountId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }


	
}