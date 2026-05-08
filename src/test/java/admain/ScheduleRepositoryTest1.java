package admain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ScheduleRepositoryTest1 {

    private scheduleRepository repo;
    private Connection mockConn;
    private PreparedStatement mockPs;
    private ResultSet mockRs;

    @BeforeEach
    void setUp() throws SQLException {
        repo = Mockito.spy(new scheduleRepository());

        // mock connection
        mockConn = mock(Connection.class);
        mockPs = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);

        // override getConnection to return mock
        doReturn(mockConn).when(repo).getConnection();

        // default PreparedStatement behavior
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
    }
// ================= determineStatus (static) - CONFIRMED =================

@Test
void testDetermineStatusConfirmed() {
    ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusHours(1);
    ZonedDateTime end = start.plusMinutes(60);

    AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
    when(slot.getMaxCapacity()).thenReturn(5);
    when(slot.getBookedCount()).thenReturn(2);

    AppointmentStatus_y status = scheduleRepository.determineStatus(start, end, 1, slot);
    assertEquals(AppointmentStatus_y.CONFIRMED, status);
}

// ================= determineStatus (DB) - CONFIRMED =================

@Test
void testDetermineStatusDB_Confirmed() throws SQLException {
    ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusHours(1);
    ZonedDateTime end = start.plusMinutes(60);

    when(mockRs.next()).thenReturn(true);
    when(mockRs.getInt("max_capacity")).thenReturn(5);
    when(mockRs.getInt("booked_count")).thenReturn(2);

    AppointmentStatus_y status = repo.determineStatus(mockConn, 1, 1, start, end);
    assertEquals(AppointmentStatus_y.CONFIRMED, status);
}

// ================= determineStatus (DB) - slot not found =================

@Test
void testDetermineStatusDB_SlotNotFound() throws SQLException {
    ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusHours(1);
    ZonedDateTime end = start.plusMinutes(60);

    when(mockRs.next()).thenReturn(false);

    assertThrows(SQLException.class, () ->
        repo.determineStatus(mockConn, 99, 1, start, end));
}

// ================= isSlotAvailable - no rows =================

@Test
void testIsSlotAvailable_noRows() throws SQLException {
    when(mockRs.next()).thenReturn(false);

    boolean result = repo.isSlotAvailable(mockConn, 99, 1);
    assertFalse(result);
}

// ================= getUserEmailByAppointment =================

@Test
void testGetUserEmailByAppointment_found() throws Exception {
    scheduleRepository spyRepo = Mockito.spy(new scheduleRepository());
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    try (MockedStatic<database_connection> mocked =
                 mockStatic(database_connection.class)) {
        mocked.when(database_connection::getConnection).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("email")).thenReturn("test@test.com");

        String email = spyRepo.getUserEmailByAppointment(1);
        assertEquals("test@test.com", email);
    }
}

@Test
void testGetUserEmailByAppointment_notFound() throws Exception {
    scheduleRepository spyRepo = Mockito.spy(new scheduleRepository());
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    try (MockedStatic<database_connection> mocked =
                 mockStatic(database_connection.class)) {
        mocked.when(database_connection::getConnection).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        String email = spyRepo.getUserEmailByAppointment(1);
        assertNull(email);
    }
}

// ================= addAppointment - invalid duration =================

@Test
void testAddAppointment_invalidDuration() throws SQLException {
    ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusHours(1);
    ZonedDateTime end = start.plusMinutes(10); // أقل من 30 دقيقة

    Appointment appt = mock(Appointment.class);
    TimeSlot slot = mock(TimeSlot.class);
    when(appt.getTimeSlot()).thenReturn(slot);
    when(slot.getId()).thenReturn(1);
    when(slot.getStart()).thenReturn(start);
    when(slot.getEnd()).thenReturn(end);
    when(appt.getParticipants()).thenReturn(1);
    when(appt.getUserId()).thenReturn(1);
    when(appt.getType()).thenReturn(AppointmentType_y.GENERAL);

    assertThrows(IllegalArgumentException.class, () -> repo.addAppointment(appt));
}

// ================= addAppointment - waitlist (no booked_count update) =================

@Test
void testAddAppointment_waitlist() throws SQLException {
    ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusHours(1);
    ZonedDateTime end = start.plusMinutes(60);

    Appointment appt = mock(Appointment.class);
    TimeSlot slot = mock(TimeSlot.class);
    when(appt.getTimeSlot()).thenReturn(slot);
    when(slot.getId()).thenReturn(1);
    when(slot.getStart()).thenReturn(start);
    when(slot.getEnd()).thenReturn(end);
    when(appt.getParticipants()).thenReturn(1);
    when(appt.getUserId()).thenReturn(1);
    when(appt.getType()).thenReturn(AppointmentType_y.GENERAL);

    // slot ممتلئ → WAITLIST
    when(mockRs.next()).thenReturn(true);
    when(mockRs.getInt("max_capacity")).thenReturn(2);
    when(mockRs.getInt("booked_count")).thenReturn(2);
    when(mockPs.executeUpdate()).thenReturn(1);

    assertDoesNotThrow(() -> repo.addAppointment(appt));
}

// ================= modifyAppointment - appointment not found =================

@Test
void testModifyAppointment_notFound() throws SQLException {
    when(mockRs.next()).thenReturn(false);

    assertThrows(SQLException.class, () ->
        repo.modifyAppointment(99, 1, 1));
}

// ================= modifyAppointment - success =================

@Test
void testModifyAppointment_success() throws SQLException {
    when(mockRs.next()).thenReturn(true);
    when(mockRs.getInt("slot_id")).thenReturn(1);
    when(mockRs.getInt("participants")).thenReturn(2);
    when(mockRs.getInt("max_capacity")).thenReturn(10);
    when(mockRs.getInt("booked_count")).thenReturn(2);
    when(mockPs.executeUpdate()).thenReturn(1);

    assertDoesNotThrow(() -> repo.modifyAppointment(1, 2, 3));
    verify(mockConn).commit();
}

// ================= getAppointments - empty =================

@Test
void testGetAppointments_empty() throws SQLException {
    when(mockRs.next()).thenReturn(false);

    List<Appointment> result = repo.getAppointments(1);
    assertTrue(result.isEmpty());
}
    @Test
    void testSlotAvailableTrue() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(2);

        boolean available = repo.isSlotAvailable(mockConn, 1, 2);
        assertTrue(available);
    }

    @Test
    void testSlotAvailableFalse() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(3);
        when(mockRs.getInt("booked_count")).thenReturn(3);

        boolean available = repo.isSlotAvailable(mockConn, 1, 1);
        assertFalse(available);
    }

    @Test
    void testDetermineStatusCompleted() {
        ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).minusHours(2);
        ZonedDateTime end = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).minusHours(1);

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(2);

        AppointmentStatus_y status = scheduleRepository.determineStatus(start, end, 1, slot);
        assertEquals(AppointmentStatus_y.COMPLETED, status);
    }

    @Test
    void testDetermineStatusOngoing() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Hebron"));
        ZonedDateTime start = now.minusMinutes(10);
        ZonedDateTime end = now.plusMinutes(50);

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(2);

        AppointmentStatus_y status = scheduleRepository.determineStatus(start, end, 1, slot);
        assertEquals(AppointmentStatus_y.ONGOING, status);
    }

    @Test
    void testDetermineStatusWaitlist() {
        ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusHours(1);
        ZonedDateTime end = start.plusMinutes(60);

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(2);
        when(slot.getBookedCount()).thenReturn(2);

        AppointmentStatus_y status = scheduleRepository.determineStatus(start, end, 1, slot);
        assertEquals(AppointmentStatus_y.WAITLIST, status);
    }

    @Test
    void testAddAppointmentConfirmed() throws SQLException {
        Appointment appt = new Appointment(
                1, 10, 1,
                new TimeSlot(1, ZonedDateTime.now().plusHours(1), ZonedDateTime.now().plusHours(2)),
                1, AppointmentStatus_y.CONFIRMED, AppointmentType_y.CONSULTATION
        );

        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(0);

        // executeUpdate mocks
        when(mockPs.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> repo.addAppointment(appt));
    }

    @Test
    void testModifyAppointmentSlotFull() throws SQLException {
        int oldSlotId = 1;
        int oldParticipants = 2;
        int newSlotId = 2;
        int newParticipants = 3;

        // old appointment data
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("slot_id")).thenReturn(oldSlotId);
        when(mockRs.getInt("participants")).thenReturn(oldParticipants);

        // new slot availability
        when(mockRs.getInt("max_capacity")).thenReturn(2);
        when(mockRs.getInt("booked_count")).thenReturn(2); // full

        SQLException ex = assertThrows(SQLException.class,
                () -> repo.modifyAppointment(1, newSlotId, newParticipants));
        assertEquals("Not enough capacity for the new slot.", ex.getMessage());
    }

    // يمكن إضافة باقي الحالات المشابهة (Edge cases, VIRTUAL, URGENT, GROUP rules, etc.)
}
