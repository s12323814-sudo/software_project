package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LANA {

    private SlotService_y service;
    private AppointmentRepository_y appointmentRepo;
    private SlotRepository_y slotRepo;
    private NotificationService_y notificationService;
    private EmailService_y emailService;

    @BeforeEach
    public void setUp() {
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

    // ================= GET AVAILABLE SLOTS =================
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
}