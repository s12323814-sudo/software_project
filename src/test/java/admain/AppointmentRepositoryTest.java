package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.*;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AppointmentRepositoryTest {

    /////////////////////////////////////////////
    @Test
    void testGetUpcomingAppointments() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mockDb = mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

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

            List<Appointment> result = repo.getUpcomingAppointments();

            assertEquals(1, result.size());
        }
    }

    /////////////////////////////////////////////
    @Test
    void testGetUserUpcomingAppointments() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mockDb = mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("appointment_id")).thenReturn(1);
            when(rs.getInt("slot_id")).thenReturn(5);
            when(rs.getInt("participants")).thenReturn(2);

            when(rs.getString("status")).thenReturn("CONFIRMED");
            when(rs.getString("type")).thenReturn("ONLINE");

            // وقت بالمستقبل عشان ما ينفلتر
            Timestamp future = Timestamp.from(Instant.now().plusSeconds(3600));

            when(rs.getTimestamp("start_time")).thenReturn(future);
            when(rs.getTimestamp("end_time")).thenReturn(future);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            List<Appointment> result = repo.getUserUpcomingAppointments(10);

            assertEquals(1, result.size());
        }
    }

    /////////////////////////////////////////////
    @Test
    void testBookConfirmed() throws Exception {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now().plusDays(1), // 🔥 مهم
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                2
        );

        when(slotRepo.findById(1)).thenReturn(slot);

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> mockDb = mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString()))
                    .thenReturn(ps)
                    .thenReturn(ps2);

            when(ps.executeUpdate()).thenReturn(1);
            when(ps2.executeUpdate()).thenReturn(1);

            AppointmentRepository_y repo = new AppointmentRepository_y(slotRepo);

            boolean result = repo.book(10, 1, 2, AppointmentType_y.ONLINE);

            assertTrue(result);
        }
    }

    /////////////////////////////////////////////
    @Test
    void testCancel() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> mockDb = mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            boolean result = repo.cancel(10, 1);

            assertTrue(result);
        }
    }

    /////////////////////////////////////////////
    @Test
    void testUpdate() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> mockDb = mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            boolean result = repo.update(10, 1, 3);

            assertTrue(result);
        }
    }
    @Test
    void testFindById() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);

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

        Appointment result = repo.findById(1, conn);

        assertNotNull(result);
        assertEquals(1, result.getAppointmentId());
    }
    @Test
    void testDelete() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        AppointmentRepository_y repo = new AppointmentRepository_y();

        repo.delete(1, conn);

       
        verify(conn).prepareStatement(anyString());
        verify(ps).setInt(1, 1);
        verify(ps).executeUpdate();
    }
}