package admain;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

        try (MockedConstruction<SlotRepository_y> ignored =
                     mockConstruction(SlotRepository_y.class,
                             (mock, context) -> {
                                 doReturn(conn).when(mock).getConnection();
                             })) {

            boolean result =
                    slot.isSlotAvailableForResource(1, 1);

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

        try (MockedConstruction<SlotRepository_y> ignored =
                     mockConstruction(SlotRepository_y.class,
                             (mock, context) -> {
                                 doReturn(conn).when(mock).getConnection();
                             })) {

            boolean result =
                    slot.isSlotAvailableForResource(1, 1);

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

        try (MockedConstruction<SlotRepository_y> ignored =
                     mockConstruction(SlotRepository_y.class,
                             (mock, context) -> {
                                 doReturn(conn).when(mock).getConnection();
                             })) {

            boolean result =
                    slot.isSlotAvailableForResource(1, 1);

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

        try (MockedConstruction<SlotRepository_y> ignored =
                     mockConstruction(SlotRepository_y.class,
                             (mock, context) -> {
                                 doReturn(conn).when(mock).getConnection();
                             })) {

            boolean result =
                    slot.isSlotAvailableForResource(1, 1);

            assertFalse(result);
        }
    }
}
