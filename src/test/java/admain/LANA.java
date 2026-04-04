package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LANA {

    private SlotService_y service;
    private AppointmentRepository_y appointmentRepo;
    private SlotRepository_y slotRepo;
    private NotificationService_y notificationService;

    @BeforeEach
    public void setUp() {
        appointmentRepo = mock(AppointmentRepository_y.class);
        slotRepo = mock(SlotRepository_y.class);
        notificationService = mock(NotificationService_y.class);
        service = new SlotService_y(appointmentRepo, slotRepo, notificationService);
    }

    // ================= Test getAvailableSlots =================
    @Test
    public void testGetAvailableSlots() {
        List<AppointmentSlot_y> slotsMock = new ArrayList<>();
        slotsMock.add(mock(AppointmentSlot_y.class));
        when(slotRepo.findAvailableSlots()).thenReturn(slotsMock);

        List<AppointmentSlot_y> result = service.getAvailableSlots();
        assertEquals(1, result.size());
    }

    // ================= Test bookAppointment success =================
    @Test
    public void testBookAppointmentSuccess() throws Exception {
        int slotId = 1;
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(0);
        when(slotRepo.findById(slotId)).thenReturn(slot);
        when(appointmentRepo.book(anyInt(), anyInt(), anyInt(), any())).thenReturn(true);

        boolean result = service.bookAppointment(10, slotId, 3, AppointmentType_y.ONLINE);
        assertTrue(result);
    }

    // ================= Test bookAppointment over capacity =================
    @Test
    public void testBookAppointmentOverCapacity() throws Exception {
        int slotId = 1;
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(4);
        when(slotRepo.findById(slotId)).thenReturn(slot);

        boolean result = service.bookAppointment(10, slotId, 2, AppointmentType_y.GENERAL);
        assertFalse(result);
    }

    // ================= Test cancelAppointment =================
    @Test
    public void testCancelAppointment() throws Exception {
        when(appointmentRepo.cancel(10, 1)).thenReturn(true);
        boolean result = service.cancelAppointment(10, 1);
        assertTrue(result);
    }

    // ================= Test updateAppointment =================
    @Test
    public void testUpdateAppointment() throws Exception {
        when(appointmentRepo.update(10, 1, 5)).thenReturn(true);
        boolean result = service.updateAppointment(10, 1, 5);
        assertTrue(result);
    }

    // ================= Test viewUserAppointments =================
    @Test
    public void testViewUserAppointments() throws Exception {
        List<Appointment> mockAppts = new ArrayList<>();
        mockAppts.add(mock(Appointment.class));
        when(appointmentRepo.getUserUpcomingAppointments(10)).thenReturn(mockAppts);

        List<Appointment> result = service.viewUserAppointments(10);
        assertEquals(1, result.size());
    }

    // ================= Test addSlot =================
    @Test
    public void testAddSlot() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        when(slotRepo.addSlot(date, start, end, 5, 1)).thenReturn(true);
        boolean result = service.addSlot(date, start, end, 5, 1);
        assertTrue(result);
    }

    // ================= Test adminCancelAppointment =================
    @Test
    public void testAdminCancelAppointment() throws Exception {
        Connection conn = mock(Connection.class);
        Appointment appt = mock(Appointment.class);
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(appt.getUserId()).thenReturn(10);
        when(appt.getSlotId()).thenReturn(1);
        when(appt.getParticipants()).thenReturn(2);

        when(appointmentRepo.findById(1, conn)).thenReturn(appt);
        when(slotRepo.findById(1)).thenReturn(slot);
        when(slot.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(slotRepo.findAvailableSlotsByDate(any())).thenReturn(new ArrayList<>());

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = service.adminCancelAppointment(1);
            assertTrue(result);

            verify(notificationService, atLeastOnce()).sendNotification(anyInt(), anyString());
        }
    }

    // ================= Test adminCancelSlot =================
    @Test
    public void testAdminCancelSlot() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement psSelect = mock(PreparedStatement.class);
        PreparedStatement psDeleteAppt = mock(PreparedStatement.class);
        PreparedStatement psDeleteSlot = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            // Mock SELECT account_id
            when(conn.prepareStatement(contains("SELECT account_id"))).thenReturn(psSelect);
            when(psSelect.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false); // no users

            // Mock DELETE appointments
            when(conn.prepareStatement(contains("DELETE FROM appointments"))).thenReturn(psDeleteAppt);
            when(psDeleteAppt.executeUpdate()).thenReturn(0);

            // Mock DELETE slot
            when(conn.prepareStatement(contains("DELETE FROM appointment_slot"))).thenReturn(psDeleteSlot);
            when(psDeleteSlot.executeUpdate()).thenReturn(1);

            boolean result = service.adminCancelSlot(1);
            assertTrue(result);
        }
    }
}