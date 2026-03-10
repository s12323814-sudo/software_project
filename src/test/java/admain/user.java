package admain;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

class user {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "12345";
    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String NEW_PASSWORD = "54321";

   
    @BeforeEach
    void setup() {
        try (Connection conn = database_connection.getConnection();
                PreparedStatement stmt  = conn.prepareStatement("DELETE FROM users WHERE username=? OR email=?")) {
            stmt.setString(1, TEST_USERNAME);
            stmt.setString(2, TEST_EMAIL);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        login_foruser_y.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
    }

    @Test
    @DisplayName("Test user registration")
    void testRegister() {
        
        boolean result = login_foruser_y.register(TEST_USERNAME + "2", TEST_PASSWORD, "another@example.com");
        assertTrue(result, "New user should be registered successfully");
    }

    @Test
    @DisplayName("Test duplicate registration fails")
    void testDuplicateRegister() {
        
        boolean result = login_foruser_y.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
        assertFalse(result, "Duplicate registration should fail");
    }

    @Test
    @DisplayName("Test successful login")
    void testLoginSuccess() {
        users_y user = login_foruser_y.login(TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(user, "Login should return a user object");
        assertEquals(TEST_USERNAME, user.getUsername(), "Username should match");
        assertEquals(TEST_EMAIL, user.getEmail(), "Email should match");
    }

    @Test
    @DisplayName("Test login fails with wrong password")
    void testLoginFail() {
        users_y user = login_foruser_y.login(TEST_USERNAME, "wrongpassword");
        assertNull(user, "Login should fail and return null");
    }

    @Test
    @DisplayName("Test email existence check")
    void testEmailExists() {
        assertTrue(login_foruser_y.emailExists(TEST_EMAIL), "Email should exist");
        assertFalse(login_foruser_y.emailExists("nonexistent@example.com"), "Email should not exist");
    }

    @Test
    @DisplayName("Test update password")
    void testUpdatePassword() {
   
        boolean updated = login_foruser_y.updatePassword(TEST_EMAIL, NEW_PASSWORD);
        assertTrue(updated, "Password should be updated");

        users_y user = login_foruser_y.login(TEST_USERNAME, NEW_PASSWORD);
        assertNotNull(user, "Login with new password should succeed");
    }
}