package admain;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AccountTest {
	@Test
	void shouldAllowValidAccount() {
	    Account_y acc = new Account_y(1, "validUser", "hash", "test@test.com", Role_y.USER);
	    assertNotNull(acc);
	}@Test
	void shouldAcceptValidEmailFormats() {
	    Account_y acc = new Account_y(1, "user123", "hash", "user@mail.com", Role_y.USER);
	    assertEquals("user@mail.com", acc.getEmail());
	}
    @Test
    void shouldThrowIfUsernameNull() {
        assertThrows(IllegalArgumentException.class, () -> new Account_y(1, null, "hash", "a@test.com", Role_y.USER));
    }

    @Test
    void shouldThrowIfUsernameTooShort() {
        assertThrows(IllegalArgumentException.class, () -> new Account_y(1, "ab", "hash", "a@test.com", Role_y.USER));
    }

    @Test
    void shouldThrowIfPasswordHashNull() {
        assertThrows(IllegalArgumentException.class, () -> new Account_y(1, "user", null, "a@test.com", Role_y.USER));
    }

    @Test
    void shouldThrowIfPasswordHashEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Account_y(1, "user", "", "a@test.com", Role_y.USER));
    }

    @Test
    void shouldThrowIfEmailNull() {
        assertThrows(IllegalArgumentException.class, () -> new Account_y(1, "user", "hash", null, Role_y.USER));
    }

    @Test
    void shouldThrowIfEmailInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Account_y(1, "user", "hash", "wrongemail", Role_y.USER));
    }

    @Test
    void shouldReturnCorrectValues() {
        Account_y acc = new Account_y(1, "user", "hash", "a@test.com", Role_y.ADMIN);

        assertEquals(1, acc.getAccountId());
        assertEquals("user", acc.getUsername());
        assertEquals("hash", acc.getPasswordHash());
        assertEquals("a@test.com", acc.getEmail());
        assertEquals(Role_y.ADMIN, acc.getRole());
    }

    @Test
    void isAdminAndIsUserTests() {
        Account_y admin = new Account_y(1, "admin", "hash", "a@test.com", Role_y.ADMIN);
        Account_y user = new Account_y(2, "user", "hash", "b@test.com", Role_y.USER);
        Account_y noRole = new Account_y(3, "guest", "hash", "c@test.com", null);

        assertTrue(admin.isAdmin());
        assertFalse(admin.isUser());

        assertTrue(user.isUser());
        assertFalse(user.isAdmin());

        assertFalse(noRole.isAdmin());
        assertFalse(noRole.isUser());
    }

    @Test
    void equalsAndHashCode() {
        Account_y a1 = new Account_y(1, "user1", "hash", "a@test.com", Role_y.USER);
        Account_y a2 = new Account_y(1, "user2", "hash2", "b@test.com", Role_y.ADMIN);
        Account_y a3 = new Account_y(2, "user3", "hash3", "c@test.com", Role_y.USER);

        // Same ID -> equals
        assertEquals(a1, a2);
        assertNotEquals(a1, a3);

        // equals with null
        assertNotEquals(a1, null);
        // equals with different class
        assertNotEquals(a1, "string");

        // hashSet uniqueness
        Set<Account_y> set = new HashSet<>();
        set.add(a1);
        set.add(a2);
        set.add(a3);
        assertEquals(2, set.size()); // a1 and a2 considered same by ID
    }

    @Test
    void toStringContainsValues() {
        Account_y acc = new Account_y(1, "user", "hash", "a@test.com", Role_y.USER);
        String str = acc.toString();
        assertTrue(str.contains("1"));
        assertTrue(str.contains("user"));
        assertTrue(str.contains("a@test.com"));
        assertTrue(str.contains("USER"));
    }
    @Test
    void shouldCreateValidAccount() {
        Account_y acc = new Account_y(1, "user123", "hash", "test@mail.com", Role_y.USER);

        assertNotNull(acc);
        assertEquals(1, acc.getAccountId());
    }@Test
    void shouldHandleNullRole() {
        Account_y acc = new Account_y(1, "user123", "hash", "test@mail.com", null);

        assertFalse(acc.isAdmin());
        assertFalse(acc.isUser());
    }@Test
    void shouldRejectEmailWithoutAt() {
        assertThrows(IllegalArgumentException.class,
                () -> new Account_y(1, "user123", "hash", "invalidemail", Role_y.USER));
    }@Test
    void shouldUpdateValuesUsingSetters() {
        Account_y acc = new Account_y(1, "user123", "hash", "test@mail.com", Role_y.USER);

        acc.setUsername("newuser");
        acc.setEmail("new@mail.com");

        assertEquals("newuser", acc.getUsername());
        assertEquals("new@mail.com", acc.getEmail());
    }
    @Test
    void testGetFutureSlots_exception() {

        SlotRepository_y repo = mock(SlotRepository_y.class);

        when(repo.findAvailableSlots())
                .thenThrow(new RuntimeException("DB error"));

        BookingSmartService service = new BookingSmartService(repo);

        AppointmentSlot_y result = service.getBestSlot();

        assertNull(result);
    }@Test
    void testBookSlot_nullSlot() {

        SlotRepository_y repo = mock(SlotRepository_y.class);

        when(repo.findById(1)).thenReturn(null);

        AppointmentRepository_y service = new AppointmentRepository_y(repo);

        assertThrows(IllegalArgumentException.class,
                () -> service.book(1, 1, 2, AppointmentType_y.GENERAL));
    }@Test
    void testSlotAvailable_exception() {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                java.time.LocalDate.now(),
                java.time.LocalTime.now(),
                java.time.LocalTime.now().plusHours(1),
                5,
                0
        );

        assertThrows(RuntimeException.class, () -> {
            slot.isSlotAvailableForResource(1, 1);
        });
    }@Test
    void testEmailReturnsNull() throws Exception {

        AppointmentRepository_y repo = new AppointmentRepository_y();

        String result = repo.getUserEmailByAppointment(999999);

        assertNull(result);
    }@Test
    void testDisplayAppointments_empty1() {
        AppointmentRepository_y.displayAppointments(java.util.List.of());
    }@Test
    void testGetUserUpcomingAppointments_filtersPast() throws Exception {

        AppointmentRepository_y repo = new AppointmentRepository_y();

        // غالباً يحتاج mock DB setup (أو ignore branch)
        List<Appointment> result = repo.getUserUpcomingAppointments(1);

        assertNotNull(result);
    }@Test
    void testDisplayAppointments_empty() {
        AppointmentRepository_y.displayAppointments(List.of());
    }@Test
    void testGetFutureSlots_handlesNullList() {

        SlotRepository_y repo = mock(SlotRepository_y.class);

        when(repo.findAvailableSlots()).thenReturn(null);

        BookingSmartService service = new BookingSmartService(repo);

        AppointmentSlot_y result = service.getNearestAvailableSlot();

        assertNull(result);
    }
    @Test
    void setterMethodsUpdateValues() {
        Account_y acc = new Account_y(1, "user", "hash", "a@test.com", Role_y.USER);

        acc.setUsername("newuser");
        acc.setPasswordHash("newhash");
        acc.setEmail("new@test.com");
        acc.setRole(Role_y.ADMIN);

        assertEquals("newuser", acc.getUsername());
        assertEquals("newhash", acc.getPasswordHash());
        assertEquals("new@test.com", acc.getEmail());
        assertEquals(Role_y.ADMIN, acc.getRole());
    }
}