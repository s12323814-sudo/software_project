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
// ================= ADMIN CANCEL APPOINTMENT =================

@Test
void test_adminCancelAppointment_dbCrash() {
    try (MockedStatic<database_connection> mocked =
                 mockStatic(database_connection.class)) {
        mocked.when(database_connection::getConnection)
                .thenThrow(new RuntimeException("DB crash"));
        assertFalse(service.adminCancelAppointment(1, 10));
    }
}

@Test
void test_adminCancelAppointment_appointmentNotFound() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    try (MockedStatic<database_connection> mocked =
                 mockStatic(database_connection.class)) {
        mocked.when(database_connection::getConnection).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false); // مش موجود

        assertFalse(service.adminCancelAppointment(1, 10));
        verify(conn).rollback();
    }
}

@Test
void test_adminCancelAppointment_unauthorized() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    try (MockedStatic<database_connection> mocked =
                 mockStatic(database_connection.class)) {
        mocked.when(database_connection::getConnection).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("account_id")).thenReturn(5);
        when(rs.getString("email")).thenReturn("user@test.com");
        when(rs.getInt("slot_admin")).thenReturn(99); // admin مختلف

        assertFalse(service.adminCancelAppointment(1, 10)); // adminId=1 بس slot_admin=99
        verify(conn).rollback();
    }
}

@Test
void test_adminCancelAppointment_deleteFails() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps1 = mock(PreparedStatement.class);
    PreparedStatement ps2 = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    try (MockedStatic<database_connection> mocked =
                 mockStatic(database_connection.class)) {
        mocked.when(database_connection::getConnection).thenReturn(conn);
        when(conn.prepareStatement(anyString()))
                .thenReturn(ps1)
                .thenReturn(ps2);
        when(ps1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("account_id")).thenReturn(5);
        when(rs.getString("email")).thenReturn("user@test.com");
        when(rs.getInt("slot_admin")).thenReturn(1);
        when(ps2.executeUpdate()).thenReturn(0); // delete فشل

        assertFalse(service.adminCancelAppointment(1, 10));
        verify(conn).rollback();
    }
}

@Test
void test_adminCancelAppointment_success_withEmail() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps1 = mock(PreparedStatement.class);
    PreparedStatement ps2 = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    try (MockedStatic<database_connection> mocked =
                 mockStatic(database_connection.class)) {
        mocked.when(database_connection::getConnection).thenReturn(conn);
        when(conn.prepareStatement(anyString()))
                .thenReturn(ps1)
                .thenReturn(ps2);
        when(ps1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("account_id")).thenReturn(5);
        when(rs.getString("email")).thenReturn("user@test.com");
        when(rs.getInt("slot_admin")).thenReturn(1);
        when(ps2.executeUpdate()).thenReturn(1); // delete نجح

        assertTrue(service.adminCancelAppointment(1, 10));
        verify(conn).commit();
        verify(notificationService).sendNotification(eq(5), anyString());
        verify(emailService).sendEmail(eq("user@test.com"), anyString(), anyString());
    }
}

@Test
void test_adminCancelAppointment_success_noEmail() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps1 = mock(PreparedStatement.class);
    PreparedStatement ps2 = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    try (MockedStatic<database_connection> mocked =
                 mockStatic(database_connection.class)) {
        mocked.when(database_connection::getConnection).thenReturn(conn);
        when(conn.prepareStatement(anyString()))
                .thenReturn(ps1)
                .thenReturn(ps2);
        when(ps1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("account_id")).thenReturn(5);
        when(rs.getString("email")).thenReturn(""); // email فاضي
        when(rs.getInt("slot_admin")).thenReturn(1);
        when(ps2.executeUpdate()).thenReturn(1);

        assertTrue(service.adminCancelAppointment(1, 10));
        verify(conn).commit();
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}

// ================= BOOKING - حالات ناقصة =================

@Test
void test_book_urgent_moreThanOne() throws Exception {
    AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
    when(slotRepo.findById(1)).thenReturn(slot);
    when(slot.getMaxCapacity()).thenReturn(10);
    when(slot.getBookedCount()).thenReturn(0);

    assertThrows(IllegalArgumentException.class, () ->
            service.bookAppointment(1, 1, 2, AppointmentType_y.URGENT));
}

@Test
void test_book_individual_success() throws Exception {
    AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
    when(slotRepo.findById(1)).thenReturn(slot);
    when(slot.getMaxCapacity()).thenReturn(10);
    when(slot.getBookedCount()).thenReturn(0);
    when(appointmentRepo.book(1, 1, 1, AppointmentType_y.INDIVIDUAL)).thenReturn(true);

    assertTrue(service.bookAppointment(1, 1, 1, AppointmentType_y.INDIVIDUAL));
}

@Test
void test_book_group_lessThanTwo() throws Exception {
    AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
    when(slotRepo.findById(1)).thenReturn(slot);
    when(slot.getMaxCapacity()).thenReturn(10);
    when(slot.getBookedCount()).thenReturn(0);

    assertThrows(IllegalArgumentException.class, () ->
            service.bookAppointment(1, 1, 1, AppointmentType_y.GROUP));
}

@Test
void test_book_virtual_ignoresCapacity() throws Exception {
    AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
    when(slotRepo.findById(1)).thenReturn(slot);
    when(slot.getMaxCapacity()).thenReturn(1);
    when(slot.getBookedCount()).thenReturn(1); // ممتلئ
    when(appointmentRepo.book(1, 1, 100, AppointmentType_y.VIRTUAL)).thenReturn(true);

    assertTrue(service.bookAppointment(1, 1, 100, AppointmentType_y.VIRTUAL));
}

@Test
void test_book_inPerson_success() throws Exception {
    AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
    when(slotRepo.findById(1)).thenReturn(slot);
    when(slot.getMaxCapacity()).thenReturn(10);
    when(slot.getBookedCount()).thenReturn(0);
    when(appointmentRepo.book(1, 1, 3, AppointmentType_y.IN_PERSON)).thenReturn(true);

    assertTrue(service.bookAppointment(1, 1, 3, AppointmentType_y.IN_PERSON));
}

@Test
void test_book_followUp_success() throws Exception {
    AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
    when(slotRepo.findById(1)).thenReturn(slot);
    when(slot.getMaxCapacity()).thenReturn(10);
    when(slot.getBookedCount()).thenReturn(0);
    when(appointmentRepo.book(1, 1, 1, AppointmentType_y.FOLLOW_UP)).thenReturn(true);

    assertTrue(service.bookAppointment(1, 1, 1, AppointmentType_y.FOLLOW_UP));
}

// ================= GET ALL APPOINTMENTS =================

@Test
void test_getAllAppointments() throws Exception {
    List<Appointment> list = List.of(mock(Appointment.class));
    when(appointmentRepo.getAllAppointments(1)).thenReturn(list);

    assertEquals(1, service.getAllAppointments(1).size());
}

// ================= GET AVAILABLE SLOTS =================

@Test
void test_getAvailableSlots() {
    List<AppointmentSlot_y> slots = List.of(mock(AppointmentSlot_y.class));
    when(slotRepo.findAvailableSlots()).thenReturn(slots);

    assertEquals(1, service.getAvailableSlots().size());
}
    @Test
    void test_adminCancelSlot_dbCrash() {

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection)
                    .thenThrow(new RuntimeException("DB crash"));

            assertFalse(service.adminCancelSlot(10));
        }
    }

    @Test
    void test_adminCancelSlot_sqlException() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenThrow(new SQLException("SQL error"));

            assertFalse(service.adminCancelSlot(10));
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

            assertFalse(service.adminCancelSlot(10));

            verify(conn).rollback();
        }
    }

    @Test
    void test_adminCancelSlot_success() throws Exception {

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
            when(ps3.executeUpdate()).thenReturn(1);

            assertTrue(service.adminCancelSlot(10));

            verify(conn).commit();
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

        assertTrue(service.bookAppointment(1, 1, 2, AppointmentType_y.GENERAL));
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

        assertTrue(service.addSlot(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                1
        ));
    }

    @Test
    void test_addSlot_invalid() {

        assertFalse(service.addSlot(
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now(),
                0,
                1
        ));
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
