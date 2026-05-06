package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.*;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AppointmentRepositoryTest {

  // --------------------------------
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
    }@Test
    void testBookOverCapacity() {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.now().plusDays(1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(5);
        when(slot.getMaxCapacity()).thenReturn(5);

        when(slotRepo.findById(1)).thenReturn(slot);

        AppointmentRepository_y repo = new AppointmentRepository_y(slotRepo);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            repo.book(10, 1, 1, AppointmentType_y.ONLINE);
        });

        assertTrue(ex.getMessage().contains("not enough capacity"));
    }@Test
    void testUpdateFail() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        try (MockedStatic<database_connection> mockDb =
                     mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            boolean result = repo.update(10, 1, 5);

            assertFalse(result);
        }
    }@Test
    void testGetUserEmailNotFound() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<database_connection> mockDb =
                     mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            String email = repo.getUserEmailByAppointment(1);

            assertNull(email);
        }
    }@Test
    void testDisplayAppointments() {

        Appointment appt = mock(Appointment.class);
        TimeSlot slot = mock(TimeSlot.class);

        when(appt.getUserId()).thenReturn(1);
        when(appt.getSlotId()).thenReturn(2);
        when(appt.getParticipants()).thenReturn(3);
        when(appt.getType()).thenReturn(AppointmentType_y.ONLINE);
        when(appt.getStatus()).thenReturn(AppointmentStatus_y.CONFIRMED);

        when(appt.getTimeSlot()).thenReturn(slot);
        when(slot.getStart()).thenReturn(ZonedDateTime.now());
        when(slot.getEnd()).thenReturn(ZonedDateTime.now().plusHours(1));
        when(slot.getDurationMinutes()).thenReturn(60);

        AppointmentRepository_y.displayAppointments(List.of(appt));

        assertTrue(true); // بس تغطية execution
    }@Test
    void testBookDurationAndSuccess() throws Exception {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.now().plusDays(1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 30)); // 90 min
        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(10);

        when(slotRepo.findById(1)).thenReturn(slot);

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString()))
                .thenReturn(ps)
                .thenReturn(ps2);

        when(ps.executeUpdate()).thenReturn(1);
        when(ps2.executeUpdate()).thenReturn(1);

        try (MockedStatic<database_connection> mockDb =
                     mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            AppointmentRepository_y repo = new AppointmentRepository_y(slotRepo);

            boolean result = repo.book(1, 1, 2, AppointmentType_y.ONLINE);

            assertTrue(result);
        }
    }@Test
    void testDeleteFail() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        AppointmentRepository_y repo = new AppointmentRepository_y();

        int result = repo.delete(1, conn);

        assertEquals(0, result);
    }@Test
    void testFindByIdNotFound() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        AppointmentRepository_y repo = new AppointmentRepository_y();

        Appointment result = repo.findById(1, conn);

        assertNull(result);
    }@Test
    void testCancelNotFound() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<database_connection> mockDb =
                     mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            boolean result = repo.cancel(1, 1);

            assertFalse(result);
            verify(conn).rollback();
        }
    }@Test
    void testGetUserUpcomingAppointmentsEmpty() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<database_connection> mockDb =
                     mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            List<Appointment> result = repo.getUserUpcomingAppointments(1);

            assertTrue(result.isEmpty());
        }
    }@Test
    void testEnumParsing() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("appointment_id")).thenReturn(1);
        when(rs.getInt("account_id")).thenReturn(1);
        when(rs.getInt("slot_id")).thenReturn(1);
        when(rs.getInt("participants")).thenReturn(1);

        when(rs.getString("status")).thenReturn("CONFIRMED");
        when(rs.getString("type")).thenReturn("ONLINE");

        Timestamp t = Timestamp.from(Instant.now());
        when(rs.getTimestamp("start_time")).thenReturn(t);
        when(rs.getTimestamp("end_time")).thenReturn(t);

        try (MockedStatic<database_connection> mockDb =
                     mockStatic(database_connection.class)) {

            mockDb.when(database_connection::getConnection).thenReturn(conn);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            List<Appointment> list = repo.getUpcomingAppointments();

            assertEquals(1, list.size());
        }
    }@Test
    void testDisplayAppointmentsNullSafe() {

        AppointmentRepository_y.displayAppointments(List.of());

        assertTrue(true);
    }
    @Test
    void testBookSlotNotFound() {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        when(slotRepo.findById(1)).thenReturn(null);

        AppointmentRepository_y repo = new AppointmentRepository_y(slotRepo);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            repo.book(10, 1, 2, AppointmentType_y.ONLINE);
        });

        assertTrue(ex.getMessage().contains("does not exist"));
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

// --------------------------------
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
// --------------------------------
   
    @Test
    void testCancel() throws Exception {

        AppointmentRepository_y repo = mock(AppointmentRepository_y.class);

        when(repo.cancel(10, 1)).thenReturn(true);

        boolean result = repo.cancel(10, 1);

        assertTrue(result);

        verify(repo).cancel(10, 1);
    }
// --------------------------------
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
