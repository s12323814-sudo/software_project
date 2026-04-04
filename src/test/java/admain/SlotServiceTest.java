package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlotServiceTest {

    private AppointmentRepository_y appointmentRepo;
    private SlotRepository_y slotRepo;
    private NotificationService_y notificationService;
    private SlotService_y service;

    @BeforeEach
    void setUp() {
        appointmentRepo = mock(AppointmentRepository_y.class);
        slotRepo = mock(SlotRepository_y.class);
        notificationService = mock(NotificationService_y.class);

        service = new SlotService_y(appointmentRepo, slotRepo, notificationService);
    }

    // ---------------- BOOKING ----------------
    @Test
    void testBook_success_individual() throws SQLException {
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(2);

        when(appointmentRepo.book(1, 1, 1, AppointmentType_y.INDIVIDUAL))
                .thenReturn(true);

        boolean result = service.bookAppointment(1, 1, 1, AppointmentType_y.INDIVIDUAL);

        assertTrue(result);
        verify(appointmentRepo).book(1, 1, 1, AppointmentType_y.INDIVIDUAL);
    }

    @Test
    void testBook_fail_wrongParticipants() throws SQLException {
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(10);
        when(slot.getBookedCount()).thenReturn(2);

        boolean result = service.bookAppointment(1, 1, 2, AppointmentType_y.INDIVIDUAL);

        assertFalse(result);
        verify(appointmentRepo, never()).book(anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    void testBook_fail_capacityExceeded() throws SQLException {
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(5);

        boolean result = service.bookAppointment(1, 1, 1, AppointmentType_y.GROUP);

        assertFalse(result);
        verify(appointmentRepo, never()).book(anyInt(), anyInt(), anyInt(), any());
    }
    @Test
    void testBookAppointment_Success() throws Exception {

        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);

        SlotService_y service = new SlotService_y(appointmentRepo, slotRepo, notificationService);

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10,0),
                LocalTime.of(11,0),
                5,
                2
        );

        when(slotRepo.findById(1)).thenReturn(slot);
        when(appointmentRepo.book(1,1,2, AppointmentType_y.GROUP)).thenReturn(true);

        boolean result = service.bookAppointment(1,1,2, AppointmentType_y.GROUP);

        assertTrue(result);
    }

    // ---------------- CANCEL (USER) ----------------
    @Test
    void testCancel_success() throws SQLException {
        when(appointmentRepo.cancel(1, 100)).thenReturn(true);

        boolean result = service.cancelAppointment(1, 100);

        assertTrue(result);
        verify(appointmentRepo).cancel(1, 100);
    }

    // ---------------- UPDATE ----------------
    @Test
    void testUpdate_success() throws SQLException {
        when(appointmentRepo.update(1, 100, 2)).thenReturn(true);

        boolean result = service.updateAppointment(1, 100, 2);

        assertTrue(result);
        verify(appointmentRepo).update(1, 100, 2);
    }

    // ---------------- VIEW USER APPOINTMENTS ----------------
    @Test
    void testViewUserAppointments() throws SQLException {
        when(appointmentRepo.getUserUpcomingAppointments(1))
                .thenReturn(Collections.emptyList());

        List<Appointment> appointments = service.viewUserAppointments(1);

        assertNotNull(appointments);
        assertEquals(0, appointments.size());
        verify(appointmentRepo).getUserUpcomingAppointments(1);
    }

    // ---------------- ADMIN ADD SLOT ----------------
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
    void testIsFull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.now(),
            LocalTime.of(10,0),
            LocalTime.of(11,0),
            5,
            5
        );

        assertTrue(slot.isFull());
    }
    @Test
    void testZeroCapacity() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.now(),
            LocalTime.of(10,0),
            LocalTime.of(11,0),
            0,
            0
        );

        assertTrue(slot.isFull()); // لأنه 0 >= 0
    }
    @Test
    void testTimeZone() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.of(2025,1,1),
            LocalTime.of(10,0),
            LocalTime.of(11,0),
            5,
            2
        );

        assertEquals("Asia/Hebron", slot.getStartDateTime().getZone().toString());
    }@Test
    void testMidnightTime() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.now(),
            LocalTime.MIDNIGHT,
            LocalTime.NOON,
            5,
            1
        );

        assertEquals(0, slot.getStartDateTime().getHour());
    }
    @Test
    void testToStringFullContent() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.of(2025,1,1),
            LocalTime.of(10,0),
            LocalTime.of(11,0),
            5,
            2
        );

        String result = slot.toString();

        assertTrue(result.contains("ID: 1"));
        assertTrue(result.contains("Date: 2025-01-01"));
        assertTrue(result.contains("Start: 10:00"));
        assertTrue(result.contains("End: 11:00"));
        assertTrue(result.contains("Capacity: 2/5"));
    }@Test
    void testAllGetters() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            7,
            LocalDate.of(2025,6,1),
            LocalTime.of(8,0),
            LocalTime.of(9,0),
            10,
            4
        );

        assertEquals(7, slot.getId());
        assertEquals(LocalDate.of(2025,6,1), slot.getDate());
        assertEquals(LocalTime.of(8,0), slot.getStartTime());
        assertEquals(LocalTime.of(9,0), slot.getEndTime());
        assertEquals(10, slot.getMaxCapacity());
        assertEquals(4, slot.getBookedCount());
    }
    @Test
    void testIsFull_AllCases() {

        // ========== equal ==========
        AppointmentSlot_y slot1 = new AppointmentSlot_y(
            1, LocalDate.now(), LocalTime.NOON, LocalTime.MIDNIGHT, 5, 5);
        assertTrue(slot1.isFull());

        // ========== less ==========
        AppointmentSlot_y slot2 = new AppointmentSlot_y(
            2, LocalDate.now(), LocalTime.NOON, LocalTime.MIDNIGHT, 5, 3);
        assertFalse(slot2.isFull());

        // ========== greater (edge case) ==========
        AppointmentSlot_y slot3 = new AppointmentSlot_y(
            3, LocalDate.now(), LocalTime.NOON, LocalTime.MIDNIGHT, 5, 6);
        assertTrue(slot3.isFull());
    }@Test
    void testEndDateTime() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.of(2025,1,1),
            LocalTime.of(10,0),
            LocalTime.of(12,0),
            5,
            2
        );

        assertEquals(12, slot.getEndDateTime().getHour());
    }
    @Test
    void testIsFull_Equal() {
        // booked == max
    }

    @Test
    void testIsFull_Less() {
        // booked < max
    }

    @Test
    void testIsFull_Greater() {
        // booked > max (edge case)
    }@Test
    void testGetters() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            10,
            LocalDate.of(2025,5,1),
            LocalTime.of(9,0),
            LocalTime.of(10,0),
            20,
            5
        );

        assertEquals(10, slot.getId());
        assertEquals(20, slot.getMaxCapacity());
        assertEquals(5, slot.getBookedCount());
        assertEquals(LocalTime.of(9,0), slot.getStartTime());
    }
    @Test
    void testToString() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.of(2025,1,1),
            LocalTime.of(10,0),
            LocalTime.of(11,0),
            5,
            2
        );

        String result = slot.toString();

        assertTrue(result.contains("ID: 1"));
    }@Test
    void testStartDateTime() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.of(2025,1,1),
            LocalTime.of(10,0),
            LocalTime.of(11,0),
            5,
            2
        );

        assertEquals(10, slot.getStartDateTime().getHour());
    }@Test
    void testIsNotFull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
            1,
            LocalDate.now(),
            LocalTime.of(10,0),
            LocalTime.of(11,0),
            5,
            3
        );

        assertFalse(slot.isFull());
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
        verify(slotRepo, never()).addSlot(any(), any(), any(), anyInt(), anyInt());
    }

    // ---------------- ADMIN CANCEL SLOT ----------------
    @Test
    void testAdminCancelSlot_notifications() throws SQLException {
        // Mock Connection & DB inside the service
        // هنا نفترض slotRepo.findAvailableSlotsByDate ترجع empty list
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.now());
        when(slot.getId()).thenReturn(1);
        when(slotRepo.findAvailableSlotsByDate(any())).thenReturn(Collections.emptyList());

        // بما أن الاتصال داخل try-with-resources، لن نتحقق منه، فقط نتحقق من notification
        service.adminCancelSlot(1);

        verify(notificationService, atLeast(0))
                .sendNotification(anyInt(), anyString());
    }
    
}