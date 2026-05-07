package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.*;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AppointmentRepository_yTestFixed {

    // ===================== GET UPCOMING =====================
    @Test
    void getUpcomingAppointments_returnsList() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> dbMock =
                     mockStatic(database_connection.class)) {

            dbMock.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("appointment_id")).thenReturn(1);
            when(rs.getInt("account_id")).thenReturn(10);
            when(rs.getInt("slot_id")).thenReturn(5);
            when(rs.getInt("participants")).thenReturn(2);

            when(rs.getString("status")).thenReturn("CONFIRMED");
            when(rs.getString("type")).thenReturn("ONLINE");

            Timestamp now = Timestamp.from(Instant.now());
            when(rs.getTimestamp("start_time")).thenReturn(now);
            when(rs.getTimestamp("end_time")).thenReturn(now);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            List<Appointment> result = repo.getUpcomingAppointments();

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ===================== USER UPCOMING =====================
    @Test
    void getUserUpcomingAppointments_returnsOnlyFuture() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> dbMock =
                     mockStatic(database_connection.class)) {

            dbMock.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("appointment_id")).thenReturn(1);
            when(rs.getInt("slot_id")).thenReturn(5);
            when(rs.getInt("participants")).thenReturn(2);

            when(rs.getString("status")).thenReturn("CONFIRMED");
            when(rs.getString("type")).thenReturn("ONLINE");

            Timestamp future = Timestamp.from(Instant.now().plusSeconds(3600));

            when(rs.getTimestamp("start_time")).thenReturn(future);
            when(rs.getTimestamp("end_time")).thenReturn(future);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            List<Appointment> result = repo.getUserUpcomingAppointments(10);

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ===================== UPDATE =====================
    @Test
    void update_returnsTrue_whenRowUpdated() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> dbMock =
                     mockStatic(database_connection.class)) {

            dbMock.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            boolean result = repo.update(10, 1, 3);

            // Assert
            assertTrue(result);
        }
    }

    @Test
    void update_returnsFalse_whenNoRowUpdated() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> dbMock =
                     mockStatic(database_connection.class)) {

            dbMock.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(0);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            boolean result = repo.update(10, 1, 3);

            // Assert
            assertFalse(result);
        }
    }

    // ===================== CANCEL NOT FOUND =====================
    @Test
    void cancel_returnsFalse_whenAppointmentNotFound() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> dbMock =
                     mockStatic(database_connection.class)) {

            dbMock.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            // Act
            boolean result = repo.cancel(1, 1);

            // Assert
            assertFalse(result);
            verify(conn).rollback();
        }
    }

    // ===================== BOOK SLOT NOT FOUND =====================
    @Test
    void book_throwsException_whenSlotNotFound() {

        // Arrange
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        when(slotRepo.findById(1)).thenReturn(null);

        AppointmentRepository_y repo = new AppointmentRepository_y(slotRepo);

        // Act + Assert
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> repo.book(10, 1, 2, AppointmentType_y.ONLINE));

        assertEquals("Slot not found", ex.getMessage());
    }
}