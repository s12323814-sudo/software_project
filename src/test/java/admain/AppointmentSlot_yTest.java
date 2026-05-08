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
void testIsSlotAvailable_returnsTrue_whenNoBooking() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(conn.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);
    when(rs.getInt(1)).thenReturn(0);

    AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.now(),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            5,
            0
    );

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
    when(rs.getInt(1)).thenReturn(1);

    AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.now(),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            5,
            0
    );

    assertFalse(slot.isSlotAvailableForResource(1, 1));
}

@Test
void testIsSlotAvailable_returnsFalse_whenRsNextFalse() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(conn.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false);

    AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.now(),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            5,
            0
    );

    assertFalse(slot.isSlotAvailableForResource(1, 1));
}

@Test
void testIsSlotAvailable_throwsRuntime_onSQLException() throws Exception {
    Connection conn = mock(Connection.class);

    when(conn.prepareStatement(anyString()))
            .thenThrow(new java.sql.SQLException("DB Error"));

    AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.now(),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            5,
            0
    );

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
    when(rs.getInt(1)).thenReturn(3);

    AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.now(),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            5,
            0
    );

    assertFalse(slot.isSlotAvailableForResource(1, 1));
}
}
