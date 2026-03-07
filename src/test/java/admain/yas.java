package admain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class yas {

    private static final String TEST_USERNAME = "testAdmin";
    private static final String TEST_PASSWORD = "123456";
    private static final String TEST_EMAIL = "testadmin@example.com";

    @BeforeAll
    static void setup() {
        
        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM admin WHERE email=?")) {

            stmt.setString(1, TEST_EMAIL);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        
        login_foradmain.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
    }

    @Test
    @Order(1)
    @DisplayName("Test Email Exists")
    void testEmailExists() {
        assertTrue(login_foradmain.emailExists(TEST_EMAIL), "Email should exist after registration");
    }

    @Test
    @Order(2)
    @DisplayName("Test Login Admin")
    void testLogin() {
        Admin admin = login_foradmain.login(TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(admin, "Login should return an Admin object");
        assertEquals(TEST_USERNAME, admin.getUsername(), "Username should match");
    }

    @Test
    @Order(3)
    @DisplayName("Test Update Password")
    void testUpdatePassword() {
        String newPass = "654321";
        boolean updated = login_foradmain.updatePassword(TEST_EMAIL, newPass);
        assertTrue(updated, "Password should be updated successfully");

        
        Admin admin = login_foradmain.login(TEST_USERNAME, newPass);
        assertNotNull(admin, "Login with new password should succeed");
    }
}