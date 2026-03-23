package admain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    void validAccount() {
        Account_y acc = new Account_y(1, "user", "hash", "test@test.com", Role_y.USER);

        assertEquals("user", acc.getUsername());
    }

    @Test
    void invalidUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Account_y(1, "ab", "hash", "test@test.com", Role_y.USER);
        });
    }

    @Test
    void invalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Account_y(1, "user", "hash", "wrong", Role_y.USER);
        });
    }

    @Test
    void invalidPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Account_y(1, "user", "", "test@test.com", Role_y.USER);
        });
    }
}