package admain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class Ll {

    // ================= adminCancelAppointment SUCCESS =================
    @Test
    void testAdminCancelAppointment_success() throws Exception {

        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);
        EmailService_y emailService = mock(EmailService_y.class);

        SlotService_y service = new SlotService_y(
                appointmentRepo,
                slotRepo,
                notificationService,
                emailService
        );

        Connection conn = mock(Connection.class);
        PreparedStatement psSelect = mock(PreparedStatement.class);
        PreparedStatement psDelete = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString()))
                    .thenReturn(psSelect)
                    .thenReturn(psDelete);

            when(psSelect.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt("account_id")).thenReturn(1);
            when(rs.getString("email")).thenReturn("test@test.com");

            when(psDelete.executeUpdate()).thenReturn(1);

            boolean result = service.adminCancelAppointment(1);

            assertTrue(result);

            verify(notificationService)
                    .sendNotification(eq(1), anyString());

            verify(emailService)
                    .sendEmail(eq("test@test.com"), anyString(), anyString());
        }
    }

    // ================= adminCancelAppointment NOT FOUND =================
    @Test
    void testAdminCancelAppointment_notFound() throws Exception {

        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);
        EmailService_y emailService = mock(EmailService_y.class);

        SlotService_y service = new SlotService_y(
                appointmentRepo,
                slotRepo,
                notificationService,
                emailService
        );

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(false);

            boolean result = service.adminCancelAppointment(1);

            assertFalse(result);
            verify(conn).rollback();
        }
    }

    // ================= adminCancelSlot SUCCESS =================
    @Test
    void testAdminCancelSlot_success() throws Exception {

        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);
        EmailService_y emailService = mock(EmailService_y.class);

        SlotService_y service = new SlotService_y(
                appointmentRepo,
                slotRepo,
                notificationService,
                emailService
        );

        Connection conn = mock(Connection.class);
        PreparedStatement psUsers = mock(PreparedStatement.class);
        PreparedStatement psDeleteAppt = mock(PreparedStatement.class);
        PreparedStatement psDeleteSlot = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString()))
                    .thenReturn(psUsers)
                    .thenReturn(psDeleteAppt)
                    .thenReturn(psDeleteSlot);

            when(psUsers.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("account_id")).thenReturn(1);

            when(psDeleteAppt.executeUpdate()).thenReturn(1);
            when(psDeleteSlot.executeUpdate()).thenReturn(1);

            boolean result = service.adminCancelSlot(10);

            assertTrue(result);

            verify(notificationService)
                    .sendNotification(eq(1), anyString());

            verify(conn).commit();
        }
    }
}