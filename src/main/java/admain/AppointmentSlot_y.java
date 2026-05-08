package admain;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentSlotTest {

    @Test
    void testIsSlotAvailableForResource_returnsTrue() throws Exception {

        AppointmentSlot_y slot = spy(new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(9,0),
                LocalTime.of(10,0),
                5,
                2
        ));

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        SlotRepository_y repo = mock(SlotRepository_y.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        // mock new SlotRepository_y()
        try (var mocked = mockConstruction(SlotRepository_y.class,
                (mock, context) -> {
                    when(mock.getConnection()).thenReturn(conn);
                })) {

            boolean result = slot.isSlotAvailableForResource(1, 1);

            assertTrue(result);
        }
    }

    @Test
    void testIsSlotAvailableForResource_returnsFalse() throws Exception {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(9,0),
                LocalTime.of(10,0),
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

        try (var mocked = mockConstruction(SlotRepository_y.class,
                (mock, context) -> {
                    when(mock.getConnection()).thenReturn(conn);
                })) {

            boolean result = slot.isSlotAvailableForResource(1, 1);

            assertFalse(result);
        }
    }

    @Test
    void testIsSlotAvailableForResource_throwsException() throws Exception {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(9,0),
                LocalTime.of(10,0),
                5,
                2
        );

        Connection conn = mock(Connection.class);

        when(conn.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB Error"));

        try (var mocked = mockConstruction(SlotRepository_y.class,
                (mock, context) -> {
                    when(mock.getConnection()).thenReturn(conn);
                })) {

            assertThrows(RuntimeException.class,
                    () -> slot.isSlotAvailableForResource(1, 1));
        }
    }
}
