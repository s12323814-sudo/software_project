package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SlotRepository_yTest9 {

    private SlotRepository_y repo;
    private Connection mockConn;
    private PreparedStatement mockPs;
    private ResultSet mockRs;

    @BeforeEach
    void setUp() throws SQLException {
        repo = Mockito.spy(new SlotRepository_y());
        mockConn = mock(Connection.class);
        mockPs = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);

        doReturn(mockConn).when(repo).getConnection();
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
    }

    // ================= findAvailableSlots =================

    @Test
    void testFindAvailableSlots_returnsAvailable() throws SQLException {
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("slot_id")).thenReturn(1);
        when(mockRs.getDate("slot_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockRs.getTime("slot_start_time")).thenReturn(Time.valueOf(LocalTime.of(10, 0)));
        when(mockRs.getTime("slot_end_time")).thenReturn(Time.valueOf(LocalTime.of(11, 0)));
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(2);

        List<AppointmentSlot_y> result = repo.findAvailableSlots();

        assertEquals(1, result.size());
    }

    @Test
    void testFindAvailableSlots_fullSlot_notReturned() throws SQLException {
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("slot_id")).thenReturn(1);
        when(mockRs.getDate("slot_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockRs.getTime("slot_start_time")).thenReturn(Time.valueOf(LocalTime.of(10, 0)));
        when(mockRs.getTime("slot_end_time")).thenReturn(Time.valueOf(LocalTime.of(11, 0)));
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(5); // ممتلئ

        List<AppointmentSlot_y> result = repo.findAvailableSlots();

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAvailableSlots_sqlException_returnsEmpty() throws SQLException {
        doThrow(new SQLException("DB error")).when(repo).getConnection();

        List<AppointmentSlot_y> result = repo.findAvailableSlots();

        assertTrue(result.isEmpty());
    }

    // ================= findById =================

    @Test
    void testFindById_found() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("slot_id")).thenReturn(1);
        when(mockRs.getDate("slot_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockRs.getTime("slot_start_time")).thenReturn(Time.valueOf(LocalTime.of(10, 0)));
        when(mockRs.getTime("slot_end_time")).thenReturn(Time.valueOf(LocalTime.of(11, 0)));
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(2);

        AppointmentSlot_y result = repo.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testFindById_notFound() throws SQLException {
        when(mockRs.next()).thenReturn(false);

        AppointmentSlot_y result = repo.findById(99);

        assertNull(result);
    }

    @Test
    void testFindById_sqlException_returnsNull() throws SQLException {
        doThrow(new SQLException("DB error")).when(repo).getConnection();

        AppointmentSlot_y result = repo.findById(1);

        assertNull(result);
    }

    // ================= findAvailableSlotsByDate =================

    @Test
    void testFindAvailableSlotsByDate_returnsSlots() throws SQLException {
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("slot_id")).thenReturn(1);
        when(mockRs.getDate("slot_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockRs.getTime("slot_start_time")).thenReturn(Time.valueOf(LocalTime.of(10, 0)));
        when(mockRs.getTime("slot_end_time")).thenReturn(Time.valueOf(LocalTime.of(11, 0)));
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(2);

        List<AppointmentSlot_y> result = repo.findAvailableSlotsByDate(LocalDate.now());

        assertEquals(1, result.size());
    }

    @Test
    void testFindAvailableSlotsByDate_empty() throws SQLException {
        when(mockRs.next()).thenReturn(false);

        List<AppointmentSlot_y> result = repo.findAvailableSlotsByDate(LocalDate.now());

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAvailableSlotsByDate_sqlException_returnsEmpty() throws SQLException {
        doThrow(new SQLException("DB error")).when(repo).getConnection();

        List<AppointmentSlot_y> result = repo.findAvailableSlotsByDate(LocalDate.now());

        assertTrue(result.isEmpty());
    }

    // ================= addSlot =================

    @Test
    void testAddSlot_success() throws SQLException {
        when(mockPs.executeUpdate()).thenReturn(1);

        boolean result = repo.addSlot(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5, 1
        );

        assertTrue(result);
    }

    @Test
    void testAddSlot_failure() throws SQLException {
        when(mockPs.executeUpdate()).thenReturn(0);

        boolean result = repo.addSlot(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5, 1
        );

        assertFalse(result);
    }

    @Test
    void testAddSlot_sqlException_returnsFalse() throws SQLException {
        doThrow(new SQLException("DB error")).when(repo).getConnection();

        boolean result = repo.addSlot(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5, 1
        );

        assertFalse(result);
    }

    // ================= decreaseBookedCount =================

    @Test
    void testDecreaseBookedCount_success() throws SQLException {
        when(mockPs.executeUpdate()).thenReturn(1);

        int result = repo.decreaseBookedCount(1, 2, mockConn);

        assertEquals(1, result);
    }
}
