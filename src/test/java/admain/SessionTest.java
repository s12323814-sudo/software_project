package admain;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

public class SessionTest {

    @Test
    void testConstructorAndGetter() {
        String hashed = BCrypt.hashpw("pass", BCrypt.gensalt());

        Account_y acc = new Account_y(
                1,
                "yasmeen",
                hashed,
                "yasmeen@email.com",
                Role_y.USER
        );

        session_y session = new session_y(acc);

        assertEquals(acc, session.getAccount());
    }

    @Test
    void testIsAdmin() {
        String hashed = BCrypt.hashpw("1234", BCrypt.gensalt());

        Account_y admin = new Account_y(
                2,
                "admin",
                hashed,
                "admin@email.com",
                Role_y.ADMIN
        );

        session_y session = new session_y(admin);

        assertTrue(session.isAdmin());
        assertFalse(session.isUser());
    }

    @Test
    void testIsUser() {
        String hashed = BCrypt.hashpw("1234", BCrypt.gensalt());

        Account_y user = new Account_y(
                3,
                "user",
                hashed,
                "user@email.com",
                Role_y.USER
        );

        session_y session = new session_y(user);

        assertTrue(session.isUser());
        assertFalse(session.isAdmin());
    }

    @Test
    void testLogoutUser() {

        String hashed = BCrypt.hashpw("1234", BCrypt.gensalt());

        Account_y user = new Account_y(
                4,
                "user",
                hashed,
                "user@email.com",
                Role_y.USER
        );

        session_y.currentUser = user;

        session_y.logoutUser();

        assertNull(session_y.currentUser);
    }

    @Test
    void testLogoutUser_WhenNoUser() {
        session_y.currentUser = null;

        assertDoesNotThrow(() -> session_y.logoutUser());
    }

    @Test
    void testLogoutAdmin() {

        String hashed = BCrypt.hashpw("1234", BCrypt.gensalt());

        Account_y admin = new Account_y(
                5,
                "admin",
                hashed,
                "admin@email.com",
                Role_y.ADMIN
        );

        session_y.currentAdmin = admin;

        session_y.logoutAdmin();

        assertNull(session_y.currentAdmin);
    }

    @Test
    void testLogoutAdmin_WhenNoAdmin() {
        session_y.currentAdmin = null;

        assertDoesNotThrow(() -> session_y.logoutAdmin());
    }
}