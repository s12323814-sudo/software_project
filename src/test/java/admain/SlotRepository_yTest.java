package admain;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SlotRepository_yTest {

    private SlotRepository_y repo;

    @BeforeEach
    void setUp() {
        repo = new SlotRepository_y();
    }

    @Test
    void testAddSlot_success() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeUpdate()).thenReturn(1);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.addSlot(
                    LocalDate.of(2025, 1, 1),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    5,
                    1
            );
            assertTrue(result);
            verify(ps).executeUpdate();
        }
    }

    @Test
    void testFindById_found() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true);
        when(rs.getInt("slot_id")).thenReturn(1);
        when(rs.getDate("slot_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("slot_start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("slot_end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

        when(ps.executeQuery()).thenReturn(rs);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            AppointmentSlot_y slot = repo.findById(1);
            assertNotNull(slot);
            assertEquals(1, slot.getId());
        }
    }

    @Test
    void testFindAvailableSlots_empty() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(false);
        when(ps.executeQuery()).thenReturn(rs);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            List<AppointmentSlot_y> list = repo.findAvailableSlots();
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }
    }

    @Test
    void testDecreaseBookedCount_executes() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            repo.decreaseBookedCount(1, 2, conn);
            verify(ps).setInt(1, 2);
            verify(ps).setInt(2, 1);
            verify(ps).executeUpdate();
        }
    }

    @Test
    void testFindAvailableSlotsByDate_returnsList() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getInt("slot_id")).thenReturn(1);
        when(rs.getDate("slot_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("slot_start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("slot_end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

        when(ps.executeQuery()).thenReturn(rs);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            List<AppointmentSlot_y> list = repo.findAvailableSlotsByDate(LocalDate.of(2025,1,1));
            assertEquals(1, list.size());
            assertEquals(1, list.get(0).getId());
        }
    }
}