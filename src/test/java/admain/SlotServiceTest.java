package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SlotServiceTest {

    private AppointmentRepository_y appointmentRepo;
    private SlotRepository_y slotRepo;
    private NotificationService_y notificationService;
    private EmailService_y emailService;
    private SlotService_y service;

    @BeforeEach
    void setUp() {
        appointmentRepo = mock(AppointmentRepository_y.class);
        slotRepo = mock(SlotRepository_y.class);
        notificationService = mock(NotificationService_y.class);
        emailService = mock(EmailService_y.class);

        service = new SlotService_y(
                appointmentRepo,
                slotRepo,
                notificationService,
                emailService
        );
    }

    // ================= ADMIN CANCEL SLOT =================

    @Test
    void test_adminCancelSlot_dbCrash() {

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection)
                    .thenThrow(new RuntimeException("DB crash"));

            boolean result = service.adminCancelSlot(10);

            assertFalse(result);
        }
    }

    @Test
    void test_adminCancelSlot_sqlException() throws Exception {

        Connection conn = mock(Connection.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString()))
                    .thenThrow(new SQLException("SQL error"));

            boolean result = service.adminCancelSlot(10);

            assertFalse(result);
        }
    }

    @Test
    void test_adminCancelSlot_deleteFails_shouldRollback() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);
        PreparedStatement ps3 = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString()))
                    .thenReturn(ps1)
                    .thenReturn(ps2)
                    .thenReturn(ps3);

            when(ps1.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("account_id")).thenReturn(1);

            when(ps2.executeUpdate()).thenReturn(1);
            when(ps3.executeUpdate()).thenReturn(0); // fail

            boolean result = service.adminCancelSlot(10);

            assertFalse(result);
            verify(conn).rollback();
        }
    }

    // ================= BOOKING =================

    @Test
    void test_book_success() throws Exception {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(2);

        when(appointmentRepo.book(1, 1, 2, AppointmentType_y.GENERAL))
                .thenReturn(true);

        boolean result = service.bookAppointment(1, 1, 2, AppointmentType_y.GENERAL);

        assertTrue(result);
    }

    @Test
    void test_book_slotNull() {

        when(slotRepo.findById(1)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                service.bookAppointment(1, 1, 1, AppointmentType_y.GENERAL));
    }

    @Test
    void test_book_overCapacity() throws Exception {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(5);

        assertThrows(IllegalArgumentException.class, () ->
                service.bookAppointment(1, 1, 1, AppointmentType_y.GROUP));
    }

    // ================= CANCEL / UPDATE =================

    @Test
    void test_cancel() throws Exception {
        when(appointmentRepo.cancel(1, 10)).thenReturn(true);
        assertTrue(service.cancelAppointment(1, 10));
    }

    @Test
    void test_update() throws Exception {
        when(appointmentRepo.update(1, 10, 5)).thenReturn(true);
        assertTrue(service.updateAppointment(1, 10, 5));
    }

    // ================= VIEW =================

    @Test
    void test_viewAppointments() throws Exception {

        List<Appointment> list = List.of(mock(Appointment.class));

        when(appointmentRepo.getUserUpcomingAppointments(1))
                .thenReturn(list);

        assertEquals(1, service.viewUserAppointments(1).size());
    }

    // ================= ADD SLOT =================

    @Test
    void test_addSlot_success() {

        when(slotRepo.addSlot(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(true);

        boolean result = service.addSlot(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                1
        );

        assertTrue(result);
    }

    @Test
    void test_addSlot_invalid() {

        boolean result = service.addSlot(
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now(),
                0,
                1
        );

        assertFalse(result);
    }

    // ================= SLOT =================

    @Test
    void test_isFull() {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                5
        );

        assertTrue(slot.isFull());
    }

    @Test
    void test_isNotFull() {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                3
        );

        assertFalse(slot.isFull());
    }
}
