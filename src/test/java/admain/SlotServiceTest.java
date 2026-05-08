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
@Test
void test_adminCancelSlot_whenDeleteFails_shouldRollbackAndReturnFalse() throws Exception {

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

        // users query
        when(psUsers.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("account_id")).thenReturn(1);

        // delete appointment success
        when(psDeleteAppt.executeUpdate()).thenReturn(1);

        // ❌ slot delete FAIL → يدخل الـ else
        when(psDeleteSlot.executeUpdate()).thenReturn(0);

        boolean result = service.adminCancelSlot(10);

        assertFalse(result);

        verify(conn).rollback();
    }
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
        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 999, AppointmentType_y.GENERAL);
        });
       
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

        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 2, AppointmentType_y.INDIVIDUAL);
        });

        verify(appointmentRepo, never())
                .book(anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void testBook_fail_capacityExceeded() throws Exception {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(5);

      
        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.GROUP);
        });
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
    void testCancel_notFound() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            AppointmentRepository_y repo = new AppointmentRepository_y();

            boolean result = repo.cancel(1, 10);

            assertFalse(result);
            verify(conn).rollback(); // ✅ مهم
        }
    }@Test
    void testAdminCancelAppointment_success() throws Exception {

        NotificationService_y notificationService = mock(NotificationService_y.class);
        EmailService_y emailService = mock(EmailService_y.class);

        SlotService_y service = new SlotService_y(
                null,
                null,
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

            // مهم جداً ترتيبهم
            when(conn.prepareStatement(anyString()))
                    .thenReturn(psSelect)   // أول query
                    .thenReturn(psDelete);  // delete

            // SELECT
            when(psSelect.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);

            when(rs.getInt("account_id")).thenReturn(1);
            when(rs.getString("email")).thenReturn("test@test.com");

            // 🔥 أهم سطر (سبب مشكلتك)
            when(rs.getInt("slot_admin")).thenReturn(10);

            // DELETE
            when(psDelete.executeUpdate()).thenReturn(1);

            // 🟢 استدعاء الميثود
            boolean result = service.adminCancelAppointment(10, 99);

            // 🟢 تحقق
            assertTrue(result);

            verify(conn).commit();

            verify(notificationService)
                    .sendNotification(eq(1), anyString());

            verify(emailService)
                    .sendEmail(eq("test@test.com"), anyString(), anyString());
        }
    }@Test
    void testSlotAvailable_returnsTrue() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        // 🔥 مهم جداً
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        YourService service = new YourService(repo);

        boolean result = service.isSlotAvailableForResource(1, 10);

        assertTrue(result);
    }@Test
    void testSlotAvailable_returnsFalse() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(3);

        YourService service = new YourService(repo);

        boolean result = service.isSlotAvailableForResource(1, 10);

        assertFalse(result);
    }
    @Test
    void testSlotAvailable_noResult() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        // ❌ ما في صف راجع من الداتابيس
        when(rs.next()).thenReturn(false);

        YourService service = new YourService(repo);

        boolean result = service.isSlotAvailableForResource(1, 10);

        assertFalse(result);
    }@Test
    void testSlotAvailable_sqlException() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        when(ps.executeQuery()).thenThrow(new SQLException("DB crash"));

        YourService service = new YourService(repo);

        assertThrows(RuntimeException.class, () -> {
            service.isSlotAvailableForResource(1, 10);
        });
    }@Test
    void testSlotAvailable_nullCountFallback() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);

        // إذا DB رجعت null أو 0 بشكل غير متوقع
        when(rs.getInt(1)).thenReturn(0);

        YourService service = new YourService(repo);

        assertTrue(service.isSlotAvailableForResource(1, 10));
    }@Test
    void testGetAvailableSlots_null() {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);
        EmailService_y emailService = mock(EmailService_y.class);

        SlotService_y service = new SlotService_y(
                appointmentRepo, slotRepo, notificationService, emailService
        );

        when(slotRepo.findAvailableSlots()).thenReturn(null);

        List<AppointmentSlot_y> result = service.getAvailableSlots();

        assertNull(result);
    }@Test
    void testBookAppointment_slotNull() throws Exception {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);
        EmailService_y emailService = mock(EmailService_y.class);

        SlotService_y service = new SlotService_y(
                appointmentRepo, slotRepo, notificationService, emailService
        );

        when(slotRepo.findById(1)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.GENERAL);
        });
    }
    @Test
    void testBookAppointment_urgent_invalid() throws Exception {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(0);

        SlotService_y service = new SlotService_y(
                appointmentRepo, slotRepo, mock(NotificationService_y.class), mock(EmailService_y.class)
        );

     
        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 2, AppointmentType_y.URGENT);
        });
    }
    @Test
    void testCancelAppointment11() throws Exception {

        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);

        when(appointmentRepo.cancel(1, 10)).thenReturn(true);

        SlotService_y service = new SlotService_y(
                appointmentRepo,
                mock(SlotRepository_y.class),
                mock(NotificationService_y.class),
                mock(EmailService_y.class)
        );

        assertTrue(service.cancelAppointment(1, 10));
    }
    @Test
    void testAddSlot_success11() {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);

        when(slotRepo.addSlot(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(true);

        SlotService_y service = new SlotService_y(
                mock(AppointmentRepository_y.class),
                slotRepo,
                mock(NotificationService_y.class),
                mock(EmailService_y.class)
        );

        boolean result = service.addSlot(
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                1
        );

        assertTrue(result);
    }@Test
    void testAddSlot_invalidCapacity1() {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);

        SlotService_y service = new SlotService_y(
                mock(AppointmentRepository_y.class),
                slotRepo,
                mock(NotificationService_y.class),
                mock(EmailService_y.class)
        );

        boolean result = service.addSlot(
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                0,
                1
        );

        assertFalse(result);
    }
    @Test
    void testGetAvailableSlots11() {

        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);
        EmailService_y emailService = mock(EmailService_y.class);

        SlotService_y service = new SlotService_y(
                appointmentRepo, slotRepo, notificationService, emailService
        );

        List<AppointmentSlot_y> mockList = List.of(mock(AppointmentSlot_y.class));

        when(slotRepo.findAvailableSlots()).thenReturn(mockList);

        List<AppointmentSlot_y> result = service.getAvailableSlots();

        assertEquals(1, result.size());
    }@Test
    void testSlotAvailable_verifyQueryExecuted() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        YourService service = new YourService(repo);

        service.isSlotAvailableForResource(1, 10);

        verify(conn).prepareStatement(anyString());
        verify(ps).setInt(1, 1);
        verify(ps).setInt(2, 10);
    }@Test
    void testSlotAvailable_throwsException() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB error"));

        YourService service = new YourService(repo);

        assertThrows(RuntimeException.class, () -> {
            service.isSlotAvailableForResource(1, 10);
        });
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

        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 2, AppointmentType_y.URGENT);
        });
        
        verify(appointmentRepo, never())
                .book(anyInt(), anyInt(), anyInt(), any());
    }

    // ========================= SLOT NOT FOUND =========================
    @Test
    void testBookAppointment_slotNotFound() throws SQLException {

        when(slotRepo.findById(1)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.GENERAL);
        });
    }

    // ========================= CAPACITY FAIL =========================
    @Test
    void testBookAppointment_notEnoughCapacity() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(4);

        when(slotRepo.findById(1)).thenReturn(slot);
        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 100, AppointmentType_y.GENERAL);
        });
    }

    // ========================= GROUP RULE =========================
    
    @Test
    void testBookAppointment_groupInvalid() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(0);

        when(slotRepo.findById(1)).thenReturn(slot);

        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.GROUP);
        });
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

            boolean result = service.adminCancelAppointment(1,0);

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
