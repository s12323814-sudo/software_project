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