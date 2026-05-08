package admain;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AppointmentSlot_yTest {

    // =========================
    // Constructor & Getters
    // =========================

    @Test
    void testConstructorAndGetters() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.of(2026, 1, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), 5, 2);

        assertEquals(1, slot.getId());
        assertEquals(LocalDate.of(2026, 1, 1), slot.getDate());
        assertEquals(LocalTime.of(10, 0), slot.getStartTime());
        assertEquals(LocalTime.of(11, 0), slot.getEndTime());
        assertEquals(5, slot.getMaxCapacity());
        assertEquals(2, slot.getBookedCount());
    }

    // =========================
    // isFull
    // =========================

    @Test
    void testIsFull_whenNotFull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 10, 2);
        assertFalse(slot.isFull());
    }

    @Test
    void testIsFull_whenExactFull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 5, 5);
        assertTrue(slot.isFull());
    }

    @Test
    void testIsFull_whenOverCapacity() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 5, 7);
        assertTrue(slot.isFull());
    }

    @Test
    void testIsFull_whenEmpty() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 10, 0);
        assertFalse(slot.isFull());
    }

    @Test
    void testIsFull_oneSpotLeft() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 5, 4);
        assertFalse(slot.isFull());
    }

    // =========================
    // ZonedDateTime
    // =========================

    @Test
    void testGetStartDateTime_notNull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.of(2026, 2, 2),
                LocalTime.of(9, 0), LocalTime.of(10, 0), 5, 1);
        assertNotNull(slot.getStartDateTime());
    }

    @Test
    void testGetEndDateTime_afterStart() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.of(2026, 2, 2),
                LocalTime.of(9, 0), LocalTime.of(10, 0), 5, 1);
        assertTrue(slot.getEndDateTime().isAfter(slot.getStartDateTime()));
    }

    @Test
    void testZoneIsHebron() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.of(2026, 1, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), 5, 1);
        assertEquals("Asia/Hebron", slot.getStartDateTime().getZone().getId());
        assertEquals("Asia/Hebron", slot.getEndDateTime().getZone().getId());
    }

    @Test
    void testDateTimeConsistency() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        AppointmentSlot_y slot = new AppointmentSlot_y(1, date, start, end, 5, 2);

        assertEquals(date, slot.getStartDateTime().toLocalDate());
        assertEquals(start, slot.getStartDateTime().toLocalTime());
        assertEquals(date, slot.getEndDateTime().toLocalDate());
        assertEquals(end, slot.getEndDateTime().toLocalTime());
    }

    // =========================
    // toString
    // =========================

    @Test
    void testToString_containsAllData() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                10, LocalDate.of(2026, 3, 3),
                LocalTime.of(8, 0), LocalTime.of(9, 0), 10, 4);

        String result = slot.toString();

        assertTrue(result.contains("ID: 10"));
        assertTrue(result.contains("2026-03-03"));
        assertTrue(result.contains("08:00"));
        assertTrue(result.contains("09:00"));
        assertTrue(result.contains("4/10"));
    }

    @Test
    void testToString_notNull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 5, 1);
        assertNotNull(slot.toString());
    }

    @Test
    void testToString_noNullString() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                5, LocalDate.of(2026, 6, 6),
                LocalTime.of(10, 0), LocalTime.of(11, 0), 5, 2);
        assertFalse(slot.toString().contains("null"));
    }

    // =========================
    // Appointment Constructor & Setters
    // =========================

    @Test
    void testAppointmentConstructorAndGetters() {
        Appointment appt = new Appointment(
                1, 10, 5, 3,
                AppointmentStatus_y.CONFIRMED,
                AppointmentType_y.ONLINE);

        assertEquals(1, appt.getAppointmentId());
        assertEquals(10, appt.getUserId());
        assertEquals(5, appt.getSlotId());
        assertEquals(3, appt.getParticipants());
        assertEquals(AppointmentStatus_y.CONFIRMED, appt.getStatus());
        assertEquals(AppointmentType_y.ONLINE, appt.getType());
    }

    @Test
    void testAppointmentSetters() {
        Appointment appt = new Appointment(
                1, 10, 5, 2,
                AppointmentStatus_y.PENDING,
                AppointmentType_y.OFFLINE);

        appt.setParticipants(6);
        appt.setStatus(AppointmentStatus_y.CONFIRMED);

        assertEquals(6, appt.getParticipants());
        assertEquals(AppointmentStatus_y.CONFIRMED, appt.getStatus());
    }

    @Test
    void testAppointmentToString() {
        Appointment appt = new Appointment(
                1, 10, 5, 3,
                AppointmentStatus_y.CONFIRMED,
                AppointmentType_y.ONLINE);

        String result = appt.toString();

        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("userId=10"));
        assertTrue(result.contains("slotId=5"));
        assertTrue(result.contains("participants=3"));
        assertTrue(result.contains("CONFIRMED"));
        assertTrue(result.contains("ONLINE"));
    }

    // =========================
    // isSlotAvailableForResource
    // =========================

    @Test
    void testIsSlotAvailable_noConnectionThrowsRuntime() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(11, 0), 5, 0);

        assertThrows(RuntimeException.class, () ->
                slot.isSlotAvailableForResource(1, 1));
    }

    @Test
    void testIsSlotAvailable_returnsTrue_whenNoBooking() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0); // مفيش حجز = متاح

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(11, 0), 5, 0, conn);

        assertTrue(slot.isSlotAvailableForResource(1, 1));
    }

    @Test
    void testIsSlotAvailable_returnsFalse_whenBooked() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1); // في حجز = مش متاح

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(11, 0), 5, 0, conn);

        assertFalse(slot.isSlotAvailableForResource(1, 1));
    }

    @Test
    void testIsSlotAvailable_returnsFalse_whenRsNextFalse() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false); // ما رجع نتيجة

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(11, 0), 5, 0, conn);

        assertFalse(slot.isSlotAvailableForResource(1, 1));
    }

    @Test
    void testIsSlotAvailable_throwsRuntime_onSQLException() throws Exception {
        Connection conn = mock(Connection.class);

        when(conn.prepareStatement(anyString())).thenThrow(new java.sql.SQLException("DB Error"));

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(11, 0), 5, 0, conn);

        assertThrows(RuntimeException.class, () ->
                slot.isSlotAvailableForResource(1, 1));
    }

    @Test
    void testIsSlotAvailable_multipleBookings_returnsFalse() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(3); // في 3 حجوزات

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.of(10, 0),
                LocalTime.of(11, 0), 5, 0, conn);

        assertFalse(slot.isSlotAvailableForResource(1, 1));
    }
}
