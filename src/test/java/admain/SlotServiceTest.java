package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    // ---------------- BOOKING ----------------
    @Test
    public void testGetAvailableSlots() {
        List<AppointmentSlot_y> slotsMock = new ArrayList<>();
        slotsMock.add(mock(AppointmentSlot_y.class));

        when(slotRepo.findAvailableSlots()).thenReturn(slotsMock);

        List<AppointmentSlot_y> result = service.getAvailableSlots();

        assertEquals(1, result.size());
    }

    // ================= BOOK SUCCESS =================
    @Test
    public void testBookAppointmentSuccess() throws Exception {
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(0);

        when(slotRepo.findById(1)).thenReturn(slot);
        when(appointmentRepo.book(anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(true);

        boolean result = service.bookAppointment(
                10, 1, 3, AppointmentType_y.GROUP
        );

        assertTrue(result);
    }

    // ================= BOOK FAIL (CAPACITY) =================
    @Test
    public void testBookAppointmentOverCapacity() throws Exception {
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(4);
        when(slotRepo.findById(1)).thenReturn(slot);

        boolean result = service.bookAppointment(
                10, 1, 2, AppointmentType_y.GENERAL
        );

        assertFalse(result);
        verify(appointmentRepo, never())
                .book(anyInt(), anyInt(), anyInt(), any());
    }

    // ================= CANCEL =================
    @Test
    public void testCancelAppointment() throws Exception {
        when(appointmentRepo.cancel(10, 1)).thenReturn(true);

        assertTrue(service.cancelAppointment(10, 1));
    }

    // ================= UPDATE =================
    @Test
    public void testUpdateAppointment() throws Exception {
        when(appointmentRepo.update(10, 1, 5)).thenReturn(true);

        assertTrue(service.updateAppointment(10, 1, 5));
    }

    // ================= VIEW =================
    @Test
    public void testViewUserAppointments() throws Exception {
        List<Appointment> list = new ArrayList<>();
        list.add(mock(Appointment.class));

        when(appointmentRepo.getUserUpcomingAppointments(10))
                .thenReturn(list);

        List<Appointment> result = service.viewUserAppointments(10);

        assertEquals(1, result.size());
    }

    // ================= ADD SLOT =================
    @Test
    public void testAddSlot() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        when(slotRepo.addSlot(date, start, end, 5, 1))
                .thenReturn(true);

        assertTrue(service.addSlot(date, start, end, 5, 1));
    }

    // ================= ADD SLOT FAIL =================
    @Test
    public void testAddSlotFail() {
        boolean result = service.addSlot(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                0,
                1
        );

        assertFalse(result);
        verify(slotRepo, never()).addSlot(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void testBook_success_individual() throws Exception {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(2);

        when(appointmentRepo.book(1, 1, 1, AppointmentType_y.INDIVIDUAL))
                .thenReturn(true);

        boolean result = service.bookAppointment(
                1, 1, 1, AppointmentType_y.INDIVIDUAL
        );

        assertTrue(result);

        verify(appointmentRepo)
                .book(1, 1, 1, AppointmentType_y.INDIVIDUAL);
    }

    @Test
    void testBook_fail_wrongParticipants() throws Exception {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(2);

        boolean result = service.bookAppointment(
                1, 1, 2, AppointmentType_y.INDIVIDUAL
        );

        assertFalse(result);

        verify(appointmentRepo, never())
                .book(anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void testBook_fail_capacityExceeded() throws Exception {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(5);

        boolean result = service.bookAppointment(
                1, 1, 1, AppointmentType_y.GROUP
        );

        assertFalse(result);

        verify(appointmentRepo, never())
                .book(anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void testBook_success_group() throws Exception {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                2
        );

        when(slotRepo.findById(1)).thenReturn(slot);
        when(appointmentRepo.book(1, 1, 2, AppointmentType_y.GROUP))
                .thenReturn(true);

        boolean result = service.bookAppointment(
                1, 1, 2, AppointmentType_y.GROUP
        );

        assertTrue(result);
    }

    // ---------------- CANCEL ----------------

    @Test
    void testCancel_success() throws Exception {

        when(appointmentRepo.cancel(1, 100)).thenReturn(true);

        boolean result = service.cancelAppointment(1, 100);

        assertTrue(result);

        verify(appointmentRepo).cancel(1, 100);
    }

    // ---------------- UPDATE ----------------

    @Test
    void testUpdate_success() throws Exception {

        when(appointmentRepo.update(1, 100, 2)).thenReturn(true);

        boolean result = service.updateAppointment(1, 100, 2);

        assertTrue(result);

        verify(appointmentRepo).update(1, 100, 2);
    }

    // ---------------- VIEW ----------------

    @Test
    void testViewUserAppointments1() throws Exception {

        when(appointmentRepo.getUserUpcomingAppointments(1))
                .thenReturn(Collections.emptyList());

        List<Appointment> list =
                service.viewUserAppointments(1);

        assertNotNull(list);
        assertEquals(0, list.size());

        verify(appointmentRepo).getUserUpcomingAppointments(1);
    }

    // ---------------- ADD SLOT ----------------

    @Test
    void testAddSlot_success() {

        when(slotRepo.addSlot(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(true);

        boolean result = service.addSlot(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                10,
                1
        );

        assertTrue(result);

        verify(slotRepo).addSlot(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void testAddSlot_fail_invalidCapacity() {

        boolean result = service.addSlot(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                0,
                1
        );

        assertFalse(result);

        verify(slotRepo, never())
                .addSlot(any(), any(), any(), anyInt(), anyInt());
    }

    // ---------------- SLOT OBJECT ----------------

    @Test
    void testIsFull() {

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
    // ========================= GET SLOTS =========================
    @Test
    void testGetAvailableSlots1() {

        List<AppointmentSlot_y> mockList = Arrays.asList(
                mock(AppointmentSlot_y.class),
                mock(AppointmentSlot_y.class)
        );

        when(slotRepo.findAvailableSlots()).thenReturn(mockList);

        List<AppointmentSlot_y> result = service.getAvailableSlots();

        assertEquals(2, result.size());
    }

    // ========================= BOOK SUCCESS =========================
    @Test
    void testBookAppointment_success() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(5);

        when(slotRepo.findById(1)).thenReturn(slot);

        when(appointmentRepo.book(1, 1, 2, AppointmentType_y.GENERAL))
                .thenReturn(true);

        boolean result = service.bookAppointment(1, 1, 2, AppointmentType_y.GENERAL);

        assertTrue(result);
        verify(appointmentRepo).book(1, 1, 2, AppointmentType_y.GENERAL);
    }

    // ========================= BOOK FAIL (URGENT RULE) =========================
    @Test
    void testBookAppointment_urgent_invalidParticipants() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(0);

        when(slotRepo.findById(1)).thenReturn(slot);

        boolean result = service.bookAppointment(
                1, 1, 2, AppointmentType_y.URGENT
        );

        assertFalse(result);
        verify(appointmentRepo, never())
                .book(anyInt(), anyInt(), anyInt(), any());
    }

    // ========================= SLOT NOT FOUND =========================
    @Test
    void testBookAppointment_slotNotFound() throws SQLException {

        when(slotRepo.findById(1)).thenReturn(null);

        boolean result = service.bookAppointment(1, 1, 2, AppointmentType_y.GENERAL);

        assertFalse(result);
    }

    // ========================= CAPACITY FAIL =========================
    @Test
    void testBookAppointment_notEnoughCapacity() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(4);

        when(slotRepo.findById(1)).thenReturn(slot);

        boolean result = service.bookAppointment(1, 1, 5, AppointmentType_y.GENERAL);

        assertFalse(result);
    }

    // ========================= GROUP RULE =========================
    @Test
    void testBookAppointment_groupInvalid() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(0);

        when(slotRepo.findById(1)).thenReturn(slot);

        boolean result = service.bookAppointment(1, 1, 1, AppointmentType_y.GROUP);

        assertFalse(result);
    }

    // ========================= ADD SLOT =========================
    @Test
    void testAddSlot_success1() {

        when(slotRepo.addSlot(any(), any(), any(), eq(10), eq(1)))
                .thenReturn(true);

        boolean result = service.addSlot(
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                10,
                1
        );

        assertTrue(result);
    }

    // ========================= INVALID SLOT =========================
    @Test
    void testAddSlot_invalidCapacity() {

        boolean result = service.addSlot(
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now(),
                0,
                1
        );

        assertFalse(result);
    }

    // ========================= CANCEL =========================
    @Test
    void testCancelAppointment1() throws SQLException {

        when(appointmentRepo.cancel(1, 10)).thenReturn(true);

        boolean result = service.cancelAppointment(1, 10);

        assertTrue(result);
        verify(appointmentRepo).cancel(1, 10);
    }

    // ========================= UPDATE =========================
    @Test
    void testUpdateAppointment1() throws SQLException {

        when(appointmentRepo.update(1, 10, 5)).thenReturn(true);

        assertTrue(service.updateAppointment(1, 10, 5));
    }

    // ========================= VIEW =========================
    @Test
    void testViewUserAppointments11() throws SQLException {

        List<Appointment> list = Arrays.asList(mock(Appointment.class));

        when(appointmentRepo.getUserUpcomingAppointments(1))
                .thenReturn(list);

        List<Appointment> result = service.viewUserAppointments(1);

        assertEquals(1, result.size());
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

    @Test
    void testIsNotFull() {

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