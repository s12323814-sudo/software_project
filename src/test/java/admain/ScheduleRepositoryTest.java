package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleRepositoryTest {

    @Mock
    Connection mockConn;

    @Mock
    PreparedStatement mockPs;

    @Mock
    ResultSet mockRs;

    scheduleRepository repo;

    @BeforeEach
    void setup() throws SQLException {
        MockitoAnnotations.openMocks(this);
        repo = new scheduleRepository();

        // إعداد Connection و PreparedStatement افتراضي
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
    }

    @Test
    void testIsSlotAvailable_true() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(10);
        when(mockRs.getInt("booked_count")).thenReturn(5);

        boolean available = repo.isSlotAvailable(mockConn, 1, 3);
        System.out.println("Slot available? " + available); // طباعه للتوضيح
        assertTrue(available);
    }

    @Test
    void testIsSlotAvailable_false() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(5);

        boolean available = repo.isSlotAvailable(mockConn, 1, 1);
        System.out.println("Slot available? " + available); // طباعه للتوضيح
        assertFalse(available);
    }

    @Test
    void testDetermineStatusCompleted() throws SQLException {
        ZonedDateTime start = ZonedDateTime.now().minusHours(2);
        ZonedDateTime end = ZonedDateTime.now().minusHours(1);

        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(2);

        AppointmentStatus_y status = repo.determineStatus(mockConn, 1, 1, start, end);
        System.out.println("Status: " + status); // طباعه للتوضيح
        assertEquals(AppointmentStatus_y.COMPLETED, status);
    }

    @Test
    void testDetermineStatusOngoing() throws SQLException {
        ZonedDateTime start = ZonedDateTime.now().minusMinutes(10);
        ZonedDateTime end = ZonedDateTime.now().plusMinutes(20);

        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(2);

        AppointmentStatus_y status = repo.determineStatus(mockConn, 1, 1, start, end);
        System.out.println("Status: " + status); // طباعه للتوضيح
        assertEquals(AppointmentStatus_y.ONGOING, status);
    }

    @Test
    void testDetermineStatusWaitlist() throws SQLException {
        ZonedDateTime start = ZonedDateTime.now().plusMinutes(10);
        ZonedDateTime end = ZonedDateTime.now().plusHours(1);

        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(5);
        when(mockRs.getInt("booked_count")).thenReturn(5);

        AppointmentStatus_y status = repo.determineStatus(mockConn, 1, 1, start, end);
        System.out.println("Status: " + status); // طباعه للتوضيح
        assertEquals(AppointmentStatus_y.WAITLIST, status);
    }
}