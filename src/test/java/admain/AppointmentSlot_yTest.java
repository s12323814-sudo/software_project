package admain;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.time.*;
import java.sql.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class AppointmentSlot_yTest {

    @Test
    void testIsSlotAvailable_returnsTrue_whenNoBooking() throws Exception {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                5,
                2
        );

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        try (MockedStatic<database_connection> dbMock =
                     mockStatic(database_connection.class)) {

            dbMock.when(database_connection::getConnection)
                  .thenReturn(conn);

            boolean result = slot.isSlotAvailableForResource(1, 1);

            assertTrue(result);
        }
    }

    @Test
    void testIsSlotAvailable_returnsFalse_whenBooked() throws Exception {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                5,
                2
        );

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(3);

        try (MockedStatic<database_connection> dbMock =
                     mockStatic(database_connection.class)) {

            dbMock.when(database_connection::getConnection)
                  .thenReturn(conn);

            boolean result = slot.isSlotAvailableForResource(1, 1);

            assertFalse(result);
        }
    }

    @Test
    void testIsSlotAvailable_returnsFalse_whenRsNextFalse() throws Exception {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                5,
                2
        );

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<database_connection> dbMock =
                     mockStatic(database_connection.class)) {

            dbMock.when(database_connection::getConnection)
                  .thenReturn(conn);

            boolean result = slot.isSlotAvailableForResource(1, 1);

            assertFalse(result);
        }
    }

    @Test
    void testIsSlotAvailable_multipleBookings_returnsFalse() throws Exception {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                5,
                5
        );

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(10);

        try (MockedStatic<database_connection> dbMock =
                     mockStatic(database_connection.class)) {

            dbMock.when(database_connection::getConnection)
                  .thenReturn(conn);

            boolean result = slot.isSlotAvailableForResource(1, 1);

            assertFalse(result);
        }
    }
    @Test
void testGetEndTime() {
    AppointmentSlot_y slot = new AppointmentSlot_y(
        1, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), 10, 2
    );
    assertEquals(LocalTime.of(10, 0), slot.getEndTime());
}
@Test
void addSlot_whenSQLException_shouldReturnFalse() throws Exception {

    Connection conn = mock(Connection.class);

    PreparedStatement ps = mock(PreparedStatement.class);

    when(conn.prepareStatement(anyString()))
            .thenReturn(ps);

    when(ps.executeUpdate())
            .thenThrow(new SQLException("DB error"));

    SlotRepository_y repo = new SlotRepository_y() {
        @Override
        protected Connection getConnection() {
            return conn;
        }
    };

    boolean result = repo.addSlot(
            LocalDate.now(),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            5,
            1
    );

    assertFalse(result);
}
@Test
void testGetStartDateTime() {
    LocalDate date = LocalDate.now();
    AppointmentSlot_y slot = new AppointmentSlot_y(
        1, date, LocalTime.of(9, 0), LocalTime.of(10, 0), 10, 2
    );
    ZonedDateTime expected = ZonedDateTime.of(date, LocalTime.of(9, 0), ZoneId.of("Asia/Hebron"));
    assertEquals(expected, slot.getStartDateTime());
}

@Test
void testGetEndDateTime() {
    LocalDate date = LocalDate.now();
    AppointmentSlot_y slot = new AppointmentSlot_y(
        1, date, LocalTime.of(9, 0), LocalTime.of(10, 0), 10, 2
    );
    ZonedDateTime expected = ZonedDateTime.of(date, LocalTime.of(10, 0), ZoneId.of("Asia/Hebron"));
    assertEquals(expected, slot.getEndDateTime());
}

@Test
void testIsSlotAvailableForResource_sqlException() throws Exception {
    AppointmentSlot_y slot = Mockito.spy(new AppointmentSlot_y(
        1, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), 10, 2
    ));
    Connection mockConn = mock(Connection.class);
    doReturn(mockConn).when(slot).getConnection();
    when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

    assertThrows(RuntimeException.class, () ->
        slot.isSlotAvailableForResource(1, 1));
}
}
