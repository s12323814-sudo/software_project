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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ScheduleRepositoryTest1 {

    private static final ZoneId ZONE = ZoneId.of("Asia/Hebron");

    private scheduleRepository repo;
    private Connection mockConn;
    private PreparedStatement mockPs;
    private ResultSet mockRs;

    @BeforeEach
    void setUp() throws SQLException {
        repo = Mockito.spy(new scheduleRepository());
        mockConn = mock(Connection.class);
        mockPs = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);
        doReturn(mockConn).when(repo).getConnection();
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
    }

    // ===== Helper لبناء AppointmentSlot_y =====
    private AppointmentSlot_y makeSlot(int capacity, int booked) {
        return new AppointmentSlot_y(1, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), capacity, booked);
    }

    // ===== Helper لبناء Appointment بـ duration معين =====
    private Appointment appointmentWithDuration(int minutes) {
        ZonedDateTime start = ZonedDateTime.now(ZONE).plusHours(1);
        ZonedDateTime end = start.plusMinutes(minutes);
        TimeSlot slot = new TimeSlot(1, start, end);
        return new Appointment(0, 1, 1, slot, 1, null, AppointmentType_y.GENERAL);
    }

    // ================= determineStatus (static) =================

    @Test
    void testDetermineStatusConfirmed() {
        ZonedDateTime start = ZonedDateTime.now(ZONE).plusHours(1);
        ZonedDateTime end = start.plusMinutes(60);
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(2);
        assertEquals(AppointmentStatus_y.CONFIRMED, scheduleRepository.determineStatus(start, end, 1, slot));
    }

    @Test
    void testDetermineStatusCompleted() {
        ZonedDateTime start = ZonedDateTime.now(ZONE).minusHours(2);
        ZonedDateTime end = ZonedDateTime.now(ZONE).minusHours(1);
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(2);
        assertEquals(AppointmentStatus_y.COMPLETED, scheduleRepository.determineStatus(start, end, 1, slot));
    }

    @Test
    void testDetermineStatusOngoing() {
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        ZonedDateTime start = now.minusMinutes(10);
        ZonedDateTime end = now.plusMinutes(50);
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(2);
        assertEquals(AppointmentStatus_y.ONGOING, scheduleRepository.determineStatus(start, end, 1, slot));
    }

    @Test
    void testDetermineStatusWaitlist() {
        ZonedDateTime start = ZonedDateTime.now(ZONE).plusHours(1);
        ZonedDateTime end = start.plusMinutes(60);
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(2);
        when(slot.getBookedCount()).thenReturn(2);
        assertEquals(AppointmentStatus_y.WAITLIST, scheduleRepository.determineStatus(start, end, 1, slot));
    }

    // ===== بدون mock - باستخدام makeSlot =====

    @Test
    void testDetermineStatus_Completed() {
        ZonedDateTime start = ZonedDateTime.now(ZONE).minusHours(3);
        ZonedDateTime end = ZonedDateTime.now(ZONE).minusHours(1);
        AppointmentSlot_y slot = makeSlot(10, 2);
        assertEquals(AppointmentStatus_y.COMPLETED, scheduleRepository.determineStatus(start, end, 2, slot));
    }

    @Test
    void testDetermineStatus_Ongoing() {
        ZonedDateTime start = ZonedDateTime.now(ZONE).minusMinutes(30);
        ZonedDateTime end = ZonedDateTime.now(ZONE).plusMinutes(30);
        AppointmentSlot_y slot = makeSlot(10, 2);
        assertEquals(AppointmentStatus_y.ONGOING, scheduleRepository.determineStatus(start, end, 2, slot));
    }

    @Test
    void testDetermineStatus_Confirmed() {
        ZonedDateTime start = ZonedDateTime.now(ZONE).plusHours(1);
        ZonedDateTime end = ZonedDateTime.now(ZONE).plusHours(2);
        AppointmentSlot_y slot = makeSlot(10, 2);
        assertEquals(AppointmentStatus_y.CONFIRMED, scheduleRepository.determineStatus(start, end, 5, slot));
    }

    @Test
    void testDetermineStatus_Waitlist() {
        ZonedDateTime start = ZonedDateTime.now(ZONE).plusHours(1);
        ZonedDateTime end = ZonedDateTime.now(ZONE).plusHours(2);
        AppointmentSlot_y slot = makeSlot(5, 4);
        assertEquals(AppointmentStatus_y.WAITLIST, scheduleRepository.determineStatus(start, end, 3, slot));
    }

    // ================= determineStatus (DB) =================

    @Test
    void testDetermineStatusDB_Confirmed() throws SQLException {
        ZonedDateTime start = ZonedDateTime.now(ZONE).plusHours(1);
        ZonedDateTime end = start.plusMinutes(60);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(2);
        assertEquals(AppointmentStatus_y.CONFIRMED, repo.determineStatus(mockConn, 1, 1, start, end));
    }

    @Test
    void testDetermineStatusDB_SlotNotFound() throws SQLException {
        ZonedDateTime start = ZonedDateTime.now(ZONE).plusHours(1);
        ZonedDateTime end = start.plusMinutes(60);
        when(mockRs.next()).thenReturn(false);
        assertThrows(SQLException.class, () -> repo.determineStatus(mockConn, 99, 1, start, end));
    }

    // ================= isSlotAvailable =================

    @Test
    void testIsSlotAvailable_noRows() throws SQLException {
        when(mockRs.next()).thenReturn(false);
        assertFalse(repo.isSlotAvailable(mockConn, 99, 1));
    }

    @Test
    void testSlotAvailableTrue() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(2);
        assertTrue(repo.isSlotAvailable(mockConn, 1, 2));
    }

    @Test
    void testSlotAvailableFalse() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(3);
        when(mockRs.getInt("booked_count")).thenReturn(3);
        assertFalse(repo.isSlotAvailable(mockConn, 1, 1));
    }

    // ================= getUserEmailByAppointment =================

    @Test
    void testGetUserEmailByAppointment_found() throws Exception {
        scheduleRepository spyRepo = Mockito.spy(new scheduleRepository());
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("email")).thenReturn("test@test.com");
            assertEquals("test@test.com", spyRepo.getUserEmailByAppointment(1));
        }
    }

    @Test
    void testGetUserEmailByAppointment_notFound() throws Exception {
        scheduleRepository spyRepo = Mockito.spy(new scheduleRepository());
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);
            assertNull(spyRepo.getUserEmailByAppointment(1));
        }
    }

    // ================= addAppointment =================

    @Test
    void testAddAppointment_InvalidDuration_TooShort() {
        assertThrows(IllegalArgumentException.class, () -> repo.addAppointment(appointmentWithDuration(10)));
    }

    @Test
    void testAddAppointment_InvalidDuration_TooLong() {
        assertThrows(IllegalArgumentException.class, () -> repo.addAppointment(appointmentWithDuration(150)));
    }

    @Test
    void testAddAppointment_invalidDuration() throws SQLException {
        ZonedDateTime start = ZonedDateTime.now(ZONE).plusHours(1);
        ZonedDateTime end = start.plusMinutes(10);
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

    @Test
    void testAddAppointment_waitlist() throws SQLException {
        ZonedDateTime start = ZonedDateTime.now(ZONE).plusHours(1);
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
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(2);
        when(mockRs.getInt("booked_count")).thenReturn(2);
        when(mockPs.executeUpdate()).thenReturn(1);
        assertDoesNotThrow(() -> repo.addAppointment(appt));
    }

    @Test
    void testAddAppointmentConfirmed() throws SQLException {
        Appointment appt = new Appointment(
                1, 10, 1,
                new TimeSlot(1, ZonedDateTime.now(ZONE).plusHours(1), ZonedDateTime.now(ZONE).plusHours(2)),
                1, AppointmentStatus_y.CONFIRMED, AppointmentType_y.CONSULTATION
        );
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(0);
        when(mockPs.executeUpdate()).thenReturn(1);
        assertDoesNotThrow(() -> repo.addAppointment(appt));
    }

    // ================= modifyAppointment =================

    @Test
    void testModifyAppointment_notFound() throws SQLException {
        when(mockRs.next()).thenReturn(false);
        assertThrows(SQLException.class, () -> repo.modifyAppointment(99, 1, 1));
    }

    @Test
    void testModifyAppointment_NotFound() {
        assertThrows(SQLException.class, () -> repo.modifyAppointment(9999, 1, 2));
    }

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

    @Test
    void testModifyAppointmentSlotFull() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("slot_id")).thenReturn(1);
        when(mockRs.getInt("participants")).thenReturn(2);
        when(mockRs.getInt("max_capacity")).thenReturn(2);
        when(mockRs.getInt("booked_count")).thenReturn(2);
        SQLException ex = assertThrows(SQLException.class,
                () -> repo.modifyAppointment(1, 2, 3));
        assertEquals("Not enough capacity for the new slot.", ex.getMessage());
    }

    // ================= getAppointments =================

    @Test
    void testGetAppointments_empty() throws SQLException {
        when(mockRs.next()).thenReturn(false);
        List<Appointment> result = repo.getAppointments(1);
        assertTrue(result.isEmpty());
    }
}
