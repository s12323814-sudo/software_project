package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AppointmentRepository_yTest {

    private SlotRepository_y slotRepo;
    private AppointmentRepository_y repo;

    @BeforeEach
    public void setup() {
        slotRepo = mock(SlotRepository_y.class);
        repo = new AppointmentRepository_y(slotRepo);
    }

    // ================= book() =================

    @Test
    public void testBookSuccess() throws Exception {
        int slotId = 1;

        // ===== Mock Slot =====
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slotRepo.findById(slotId)).thenReturn(slot);

        // ===== Mock Connection + PreparedStatements =====
        Connection conn = mock(Connection.class);
        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        when(psInsert.executeUpdate()).thenReturn(1);
        when(psUpdate.executeUpdate()).thenReturn(1);
        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        // ===== Mock static connection + run book =====
      
    }

    @Test
    public void testBookSlotNotExist() {
        when(slotRepo.findById(999)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                repo.book(10, 999, 2, AppointmentType_y.ONLINE)
        );
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    public void testBookSlotOverCapacity() throws Exception {
        int slotId = 1;
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(4); // موجود بالفعل 4
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slotRepo.findById(slotId)).thenReturn(slot);

        Connection conn = mock(Connection.class);
        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        when(psInsert.executeUpdate()).thenReturn(1);
        when(psUpdate.executeUpdate()).thenReturn(1);
        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

          
        }
    }

    // ================= cancel() =================

    @Test
    void testAdminCancelAppointment_success() throws Exception {

        // Mock dependencies
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

        // Mock DB objects
        Connection conn = mock(Connection.class);
        PreparedStatement psSelect = mock(PreparedStatement.class);
        PreparedStatement psDelete = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection)
                  .thenReturn(conn);

            // SELECT query
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(psSelect);
            when(psSelect.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt("account_id")).thenReturn(10);
            when(rs.getString("email")).thenReturn("test@gmail.com");

            // DELETE query
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(psDelete);
            when(psDelete.executeUpdate()).thenReturn(1);

            // Services mock behavior
            doNothing().when(notificationService)
                    .sendNotification(anyInt(), anyString());

            doNothing().when(emailService)
                    .sendEmail(anyString(), anyString(), anyString());

            // execute
            boolean result = service.adminCancelAppointment(1);

            // assert
            assertTrue(result);

            verify(notificationService)
                    .sendNotification(eq(10), anyString());

            verify(emailService)
                    .sendEmail(eq("test@gmail.com"), anyString(), anyString());
        }
    }

    // ================= update() =================
    @Test
    public void testUpdate() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeUpdate()).thenReturn(1);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.update(10, 1, 5);
            assertTrue(result);
        }
    }
}