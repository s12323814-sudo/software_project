package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class New3 {

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

    // ========================= GET SLOTS =========================
    @Test
    void testGetAvailableSlots() {

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
    void testAddSlot_success() {

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
    void testCancelAppointment() throws SQLException {

        when(appointmentRepo.cancel(1, 10)).thenReturn(true);

        boolean result = service.cancelAppointment(1, 10);

        assertTrue(result);
        verify(appointmentRepo).cancel(1, 10);
    }

    // ========================= UPDATE =========================
    @Test
    void testUpdateAppointment() throws SQLException {

        when(appointmentRepo.update(1, 10, 5)).thenReturn(true);

        assertTrue(service.updateAppointment(1, 10, 5));
    }

    // ========================= VIEW =========================
    @Test
    void testViewUserAppointments() throws SQLException {

        List<Appointment> list = Arrays.asList(mock(Appointment.class));

        when(appointmentRepo.getUserUpcomingAppointments(1))
                .thenReturn(list);

        List<Appointment> result = service.viewUserAppointments(1);

        assertEquals(1, result.size());
    }
}