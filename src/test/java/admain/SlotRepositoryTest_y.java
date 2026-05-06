package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class SlotRepositoryTest_y {

    @Test
    void testFindAvailableSlots() throws Exception {

        // mocks
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        // mock static database_connection
        try (MockedStatic<database_connection> mockDb = mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            // simulate result set
            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("slot_id")).thenReturn(1);
            when(rs.getDate("slot_date")).thenReturn(Date.valueOf(LocalDate.now()));
            when(rs.getTime("slot_start_time")).thenReturn(Time.valueOf(LocalTime.of(10, 0)));
            when(rs.getTime("slot_end_time")).thenReturn(Time.valueOf(LocalTime.of(11, 0)));
            when(rs.getInt("max_capacity")).thenReturn(5);
            when(rs.getInt("booked_count")).thenReturn(2);

            SlotRepository_y repo = new SlotRepository_y();

            List<AppointmentSlot_y> result = repo.findAvailableSlots();

            assertEquals(1, result.size());
        }
    }

// --------------------------------

    @Test
    void testFindById() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mockDb = mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);

            when(rs.getInt("slot_id")).thenReturn(1);
            when(rs.getDate("slot_date")).thenReturn(Date.valueOf(LocalDate.now()));
            when(rs.getTime("slot_start_time")).thenReturn(Time.valueOf(LocalTime.of(10, 0)));
            when(rs.getTime("slot_end_time")).thenReturn(Time.valueOf(LocalTime.of(11, 0)));
            when(rs.getInt("max_capacity")).thenReturn(5);
            when(rs.getInt("booked_count")).thenReturn(1);

            SlotRepository_y repo = new SlotRepository_y();

            AppointmentSlot_y slot = repo.findById(1);

            assertNotNull(slot);
            assertEquals(1, slot.getId());
        }
    }

    // --------------------------------

    @Test
    void testAddSlot() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> mockDb = mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);

            SlotRepository_y repo = new SlotRepository_y();

            boolean result = repo.addSlot(
                    LocalDate.now(),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    5,
                    1
            );

            assertTrue(result);
        }
    }

  // --------------------------------
    @Test
    void testFindAvailableSlotsByDate() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mockDb = mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("slot_id")).thenReturn(2);
            when(rs.getDate("slot_date")).thenReturn(Date.valueOf(LocalDate.now()));
            when(rs.getTime("slot_start_time")).thenReturn(Time.valueOf(LocalTime.of(12, 0)));
            when(rs.getTime("slot_end_time")).thenReturn(Time.valueOf(LocalTime.of(13, 0)));
            when(rs.getInt("max_capacity")).thenReturn(10);
            when(rs.getInt("booked_count")).thenReturn(5);

            SlotRepository_y repo = new SlotRepository_y();

            List<AppointmentSlot_y> result =
                    repo.findAvailableSlotsByDate(LocalDate.now());

            assertEquals(1, result.size());
        }
    }
}
