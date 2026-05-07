package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.*;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AppointmentRepositoryTest {

    // ===================== GET UPCOMING =====================
    @Test
    void getUpcomingAppointments_success() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("appointment_id")).thenReturn(1);
            when(rs.getInt("account_id")).thenReturn(10);
            when(rs.getInt("slot_id")).thenReturn(5);
            when(rs.getInt("participants")).thenReturn(2);

            when(rs.getString("status")).thenReturn("CONFIRMED");
            when(rs.getString("type")).thenReturn("ONLINE");

            Timestamp t = Timestamp.from(Instant.now());
            when(rs.getTimestamp("start_time")).thenReturn(t);
            when(rs.getTimestamp("end_time")).thenReturn(t);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            List<Appointment> result = repo.getUpcomingAppointments();

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ===================== USER UPCOMING =====================
    @Test
    void getUserUpcomingAppointments_success() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("appointment_id")).thenReturn(1);
            when(rs.getInt("slot_id")).thenReturn(5);
            when(rs.getInt("participants")).thenReturn(2);

            when(rs.getString("status")).thenReturn("CONFIRMED");
            when(rs.getString("type")).thenReturn("ONLINE");

            Timestamp future =
                    Timestamp.from(Instant.now().plusSeconds(3600));

            when(rs.getTimestamp("start_time")).thenReturn(future);
            when(rs.getTimestamp("end_time")).thenReturn(future);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            List<Appointment> result =
                    repo.getUserUpcomingAppointments(10);

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ===================== BOOK SLOT NOT FOUND =====================
    @Test
    void book_slotNotFound_shouldThrowException() {

        // Arrange
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        when(slotRepo.findById(1)).thenReturn(null);

        AppointmentRepository_y repo =
                new AppointmentRepository_y(slotRepo);

        // Act + Assert
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () ->
                        repo.book(10, 1, 2, AppointmentType_y.ONLINE)
                );

        assertEquals("Slot not found", ex.getMessage());
    }

    // ===================== BOOK OVER CAPACITY =====================
    @Test
    void book_overCapacity_shouldThrowException() {

        // Arrange
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.now().plusDays(1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(5);
        when(slot.getMaxCapacity()).thenReturn(5);

        when(slotRepo.findById(1)).thenReturn(slot);

        AppointmentRepository_y repo =
                new AppointmentRepository_y(slotRepo);

        // Act + Assert
        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () ->
                        repo.book(10, 1, 1, AppointmentType_y.ONLINE)
                );

        assertTrue(ex.getMessage().contains("No capacity"));
    }

    // ===================== UPDATE SUCCESS =====================
    @Test
    void update_success_returnsTrue() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);

            AppointmentRepository_y repo =
                    new AppointmentRepository_y();

            // Act
            boolean result = repo.update(10, 1, 3);

            // Assert
            assertTrue(result);
        }
    }

    // ===================== UPDATE FAIL =====================
    @Test
    void update_fail_returnsFalse() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(0);

            AppointmentRepository_y repo =
                    new AppointmentRepository_y();

            // Act
            boolean result = repo.update(10, 1, 3);

            // Assert
            assertFalse(result);
        }
    }

    // ===================== CANCEL NOT FOUND =====================
    @Test
    void cancel_notFound_shouldReturnFalse() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> db =
                     mockStatic(database_connection.class)) {

            db.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            AppointmentRepository_y repo =
                    new AppointmentRepository_y();

            // Act
            boolean result = repo.cancel(1, 1);

            // Assert
            assertFalse(result);
            verify(conn).rollback();
        }
    }
}