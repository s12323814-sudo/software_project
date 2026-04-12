package admain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Role_yTest {

    // ================= VALID VALUES =================

    @Test
    void fromString_validUserUpperCase() {
        assertEquals(Role_y.USER, Role_y.fromString("USER"));
    }

    @Test
    void fromString_validAdminUpperCase() {
        assertEquals(Role_y.ADMIN, Role_y.fromString("ADMIN"));
    }

    @Test
    void fromString_shortUser() {
        assertEquals(Role_y.USER, Role_y.fromString("U"));
    }

    @Test
    void fromString_shortAdmin() {
        assertEquals(Role_y.ADMIN, Role_y.fromString("A"));
    }

    @Test
    void fromString_withSpaces() {
        assertEquals(Role_y.USER, Role_y.fromString("  user  "));
    }

    @Test
    void fromString_mixedCase() {
        assertEquals(Role_y.ADMIN, Role_y.fromString("AdMiN"));
    }

    // ================= INVALID VALUES =================

    @Test
    void fromString_invalidRole() {
        assertThrows(IllegalArgumentException.class,
                () -> Role_y.fromString("MANAGER"));
    }

    @Test
    void fromString_emptyString() {
        assertThrows(IllegalArgumentException.class,
                () -> Role_y.fromString(""));
    }

    @Test
    void fromString_nullValue() {
        assertThrows(IllegalArgumentException.class,
                () -> Role_y.fromString(null));
    }

    @Test
    void fromString_randomValue() {
        assertThrows(IllegalArgumentException.class,
                () -> Role_y.fromString("xyz"));
    }
}