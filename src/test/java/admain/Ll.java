package admain;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Ll {

    @Test
    void testAdminCancelAppointment_success() throws Exception {

        // Mock repositories
        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);

        SlotService_y service = new SlotService_y(appointmentRepo, slotRepo, notificationService);

        // Mock DB
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mockedStatic =
                     mockStatic(database_connection.class)) {

            mockedStatic.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);

            // Mock appointmentRepo
            Appointment appointment = mock(Appointment.class);
            when(appointment.getUserId()).thenReturn(1);
            when(appointment.getSlotId()).thenReturn(10);
            when(appointment.getParticipants()).thenReturn(2);

            when(appointmentRepo.findById(eq(1), eq(conn))).thenReturn(appointment);

            // Mock slotRepo
            AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
            when(slotRepo.findById(10)).thenReturn(slot);

            when(slotRepo.findAvailableSlotsByDate(any()))
                    .thenReturn(java.util.Collections.emptyList());

            doNothing().when(notificationService)
                    .sendNotification(anyInt(), anyString());

            // Execute
            boolean result = service.adminCancelAppointment(1);

            assertTrue(result);

            verify(notificationService, atLeastOnce())
                    .sendNotification(anyInt(), anyString());
        }
        
    }
    @Test
    void testAdminCancelAppointment_notFound() throws Exception {

        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);

        SlotService_y service = new SlotService_y(appointmentRepo, slotRepo, notificationService);

        Connection conn = mock(Connection.class);

        try (MockedStatic<database_connection> mockedStatic =
                     mockStatic(database_connection.class)) {

            mockedStatic.when(database_connection::getConnection).thenReturn(conn);

            when(appointmentRepo.findById(eq(1), eq(conn))).thenReturn(null);

            boolean result = service.adminCancelAppointment(1);

            assertFalse(result);
            verify(conn).rollback();
        }
    }@Test
    void testAdminCancelSlot_success() throws Exception {

        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);

        SlotService_y service = new SlotService_y(appointmentRepo, slotRepo, notificationService);

        Connection conn = mock(Connection.class);
        PreparedStatement psUsers = mock(PreparedStatement.class);
        PreparedStatement psDelete = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mockedStatic =
                     mockStatic(database_connection.class)) {

            mockedStatic.when(database_connection::getConnection).thenReturn(conn);

            // Users query
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(psUsers);
            when(psUsers.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("account_id")).thenReturn(1);

            // Delete queries
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(psDelete);
            when(psDelete.executeUpdate()).thenReturn(1);

            boolean result = service.adminCancelSlot(10);

            assertTrue(result);

            verify(notificationService).sendNotification(eq(1), anyString());
            verify(conn).commit();
        }
    }
}