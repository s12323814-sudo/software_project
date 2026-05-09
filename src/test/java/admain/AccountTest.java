package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.*;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AccountTest {
@Test
void testSetUsername_null() {
    Account_y acc = new Account_y(1, "user", "hash123", "user@mail.com", Role_y.USER);
    assertThrows(IllegalArgumentException.class, () -> acc.setUsername(null));
}

@Test
void testSetUsername_tooShort() {
    Account_y acc = new Account_y(1, "user", "hash123", "user@mail.com", Role_y.USER);
    assertThrows(IllegalArgumentException.class, () -> acc.setUsername("ab"));
}

@Test
void testSetPasswordHash_null() {
    Account_y acc = new Account_y(1, "user", "hash123", "user@mail.com", Role_y.USER);
    assertThrows(IllegalArgumentException.class, () -> acc.setPasswordHash(null));
}

@Test
void testSetPasswordHash_empty() {
    Account_y acc = new Account_y(1, "user", "hash123", "user@mail.com", Role_y.USER);
    assertThrows(IllegalArgumentException.class, () -> acc.setPasswordHash(""));
}

@Test
void testSetEmail_null() {
    Account_y acc = new Account_y(1, "user", "hash123", "user@mail.com", Role_y.USER);
    assertThrows(IllegalArgumentException.class, () -> acc.setEmail(null));
}

@Test
void testSetEmail_invalid() {
    Account_y acc = new Account_y(1, "user", "hash123", "user@mail.com", Role_y.USER);
    assertThrows(IllegalArgumentException.class, () -> acc.setEmail("invalidemail"));
}

@Test
void testSetRole() {
    Account_y acc = new Account_y(1, "user", "hash123", "user@mail.com", Role_y.USER);
    acc.setRole(Role_y.ADMIN);
    assertEquals(Role_y.ADMIN, acc.getRole());
}

	    // =========================
	    // 1. Constructor valid case
	    // =========================
	    @Test
	    void testCreateValidAccount() {

	        Account_y acc = new Account_y(
	                1,
	                "yasmine",
	                "hashed123",
	                "test@gmail.com",
	                Role_y.USER
	        );

	        assertEquals(1, acc.getAccountId());
	        assertEquals("yasmine", acc.getUsername());
	        assertEquals("hashed123", acc.getPasswordHash());
	        assertEquals("test@gmail.com", acc.getEmail());
	        assertEquals(Role_y.USER, acc.getRole());
	    }

	    // =========================
	    // 2. Invalid username
	    // =========================
	    @Test
	    void testInvalidUsernameThrowsException() {

	        IllegalArgumentException ex = assertThrows(
	                IllegalArgumentException.class,
	                () -> new Account_y(
	                        1,
	                        "ab",   // أقل من 3 أحرف
	                        "hashed",
	                        "test@gmail.com",
	                        Role_y.USER
	                )
	        );

	        assertEquals("Invalid username", ex.getMessage());
	    }

	    // =========================
	    // 3. Invalid password
	    // =========================
	    @Test
	    void testInvalidPasswordThrowsException() {

	        IllegalArgumentException ex = assertThrows(
	                IllegalArgumentException.class,
	                () -> new Account_y(
	                        1,
	                        "yasmine",
	                        "",
	                        "test@gmail.com",
	                        Role_y.USER
	                )
	        );

	        assertEquals("Invalid password hash", ex.getMessage());
	    }

	    // =========================
	    // 4. Invalid email
	    // =========================
	    @Test
	    void testInvalidEmailThrowsException() {

	        IllegalArgumentException ex = assertThrows(
	                IllegalArgumentException.class,
	                () -> new Account_y(
	                        1,
	                        "yasmine",
	                        "hashed",
	                        "invalidEmail",
	                        Role_y.USER
	                )
	        );

	        assertEquals("Invalid email", ex.getMessage());
	    }

	    // =========================
	    // 5. isAdmin / isUser
	    // =========================
	    @Test
	    void testRoleChecks() {

	        Account_y admin = new Account_y(
	                1, "admin", "hash", "admin@gmail.com", Role_y.ADMIN
	        );

	        Account_y user = new Account_y(
	                2, "user", "hash", "user@gmail.com", Role_y.USER
	        );

	        assertTrue(admin.isAdmin());
	        assertFalse(admin.isUser());

	        assertTrue(user.isUser());
	        assertFalse(user.isAdmin());
	    }

	    // =========================
	    // 6. setters validation
	    // =========================
	    @Test
	    void testSettersValidation() {

	        Account_y acc = new Account_y(
	                1, "yasmine", "hash", "test@gmail.com", Role_y.USER
	        );

	        // username invalid
	        assertThrows(IllegalArgumentException.class,
	                () -> acc.setUsername("ab"));

	        // password invalid
	        assertThrows(IllegalArgumentException.class,
	                () -> acc.setPasswordHash(""));

	        // email invalid
	        assertThrows(IllegalArgumentException.class,
	                () -> acc.setEmail("wrongEmail"));
	    }

	    // =========================
	    // 7. equals & hashCode
	    // =========================
	    @Test
	    void testEqualsAndHashCode() {

	        Account_y a1 = new Account_y(
	                1, "user1", "hash", "a@gmail.com", Role_y.USER
	        );

	        Account_y a2 = new Account_y(
	                1, "user2", "hash2", "b@gmail.com", Role_y.ADMIN
	        );

	        assertEquals(a1, a2); // نفس accountId
	        assertEquals(a1.hashCode(), a2.hashCode());
	    }

	    // =========================
	    // 8. toString check
	    // =========================
	    @Test
	    void testToString() {

	        Account_y acc = new Account_y(
	                1, "yasmine", "hash", "test@gmail.com", Role_y.USER
	        );

	        String result = acc.toString();

	        assertTrue(result.contains("yasmine"));
	        assertTrue(result.contains("test@gmail.com"));
	        assertTrue(result.contains("USER"));
	        assertFalse(result.contains("password")); // مهم أمنيًا
	    }
	

    // =========================
    // GET UPCOMING APPOINTMENTS
    // =========================
    @Test
    void testGetUpcomingAppointments() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("appointment_id")).thenReturn(1);
            when(rs.getInt("account_id")).thenReturn(10);
            when(rs.getInt("slot_id")).thenReturn(5);
            when(rs.getInt("participants")).thenReturn(2);

            when(rs.getString("status")).thenReturn("CONFIRMED");
            when(rs.getString("type")).thenReturn("ONLINE");

            Timestamp t = Timestamp.from(Instant.now());
            when(rs.getTimestamp("start_time")).thenReturn(t);
            when(rs.getTimestamp("end_time")).thenReturn(t);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            List<Appointment> result = repo.getUpcomingAppointments();

            // Assert
            assertEquals(1, result.size());
        }
    }

    // =========================
    // BOOK - OVER CAPACITY
    // =========================
    @Test
    void testBookOverCapacity() {

        // Arrange
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.now().plusDays(1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(5);
        when(slot.getMaxCapacity()).thenReturn(5);

        when(slotRepo.findById(1)).thenReturn(slot);

        AppointmentRepository_y repo = new AppointmentRepository_y(slotRepo);

        // Act + Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> repo.book(10, 1, 1, AppointmentType_y.ONLINE));

        assertEquals("No capacity", ex.getMessage());
    }

    // =========================
    // UPDATE FAIL
    // =========================
    @Test
    void testUpdateFail() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(0);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            boolean result = repo.update(10, 1, 5);

            // Assert
            assertFalse(result);
        }
    }

    // =========================
    // BOOK SLOT NOT FOUND
    // =========================
    @Test
    void testBookSlotNotFound() {

        // Arrange
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        when(slotRepo.findById(1)).thenReturn(null);

        AppointmentRepository_y repo = new AppointmentRepository_y(slotRepo);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repo.book(10, 1, 2, AppointmentType_y.ONLINE));

        assertTrue(ex.getMessage().contains("Slot not found"));
    }

    // =========================
    // GET USER UPCOMING
    // =========================
    @Test
    void testGetUserUpcomingAppointments() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("appointment_id")).thenReturn(1);
            when(rs.getInt("slot_id")).thenReturn(5);
            when(rs.getInt("account_id")).thenReturn(10);
            when(rs.getInt("participants")).thenReturn(2);

            when(rs.getString("status")).thenReturn("CONFIRMED");
            when(rs.getString("type")).thenReturn("ONLINE");

            Timestamp future =
                    Timestamp.from(Instant.now().plusSeconds(3600));

            when(rs.getTimestamp("start_time")).thenReturn(future);
            when(rs.getTimestamp("end_time")).thenReturn(future);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            List<Appointment> result = repo.getUserUpcomingAppointments(10);

            // Assert
            assertEquals(1, result.size());
        }
    }

    // =========================
    // BOOK SUCCESS
    // =========================
    @Test
    void testBookSuccess() throws Exception {

        // Arrange
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.now().plusDays(1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(10);

        when(slotRepo.findById(1)).thenReturn(slot);

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString()))
                    .thenReturn(ps)
                    .thenReturn(ps2);

            when(ps.executeUpdate()).thenReturn(1);
            when(ps2.executeUpdate()).thenReturn(1);

            AppointmentRepository_y repo =
                    new AppointmentRepository_y(slotRepo);

            // Act
            boolean result = repo.book(1, 1, 2, AppointmentType_y.ONLINE);

            // Assert
            assertTrue(result);
        }
    }

    // =========================
    // CANCEL NOT FOUND
    // =========================
    @Test
    void testCancelNotFound() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            AppointmentRepository_y repo =
                    new AppointmentRepository_y();

            boolean result = repo.cancel(1, 1);

            assertFalse(result);
        }
    }

    // =========================
    // UPDATE SUCCESS
    // =========================
    @Test
    void testUpdateSuccess() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);

            AppointmentRepository_y repo =
                    new AppointmentRepository_y();

            boolean result = repo.update(10, 1, 3);

            assertTrue(result);
        }
    }
}
