
package admain;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class AppointmentServiceTest {

    @Mock private Connection mockConn;
    @Mock private SlotService_y mockSlotService;
    @Mock private scheduleRepository mockScheduleRepo;

    @Mock private PreparedStatement mockSelectPs;
    @Mock private PreparedStatement mockInsertPs;
    @Mock private PreparedStatement mockUpdatePs;
    @Mock private ResultSet mockRs;

    private AppointmentService appointmentService;

    private final int userId = 1;
    private final int slotId = 10;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        appointmentService = new AppointmentService(mockConn, mockSlotService, mockScheduleRepo);

        // 🔥 مهم جدًا
        doNothing().when(mockConn).setAutoCommit(false);
        doNothing().when(mockConn).commit();
        doNothing().when(mockConn).rollback();
        doNothing().when(mockConn).close();
    }

    /** ============================
     * Helper
     * ============================
     */
    private void setupMockSlot(LocalDateTime start, LocalDateTime end, int capacity, int booked) throws Exception {

        when(mockConn.prepareStatement(startsWith("SELECT"))).thenReturn(mockSelectPs);
        when(mockConn.prepareStatement(startsWith("INSERT"))).thenReturn(mockInsertPs);
        when(mockConn.prepareStatement(startsWith("UPDATE"))).thenReturn(mockUpdatePs);

        // SELECT behavior
        when(mockSelectPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);

        // 🔥 المهم
        when(mockRs.getDate("start_date"))
            .thenReturn(java.sql.Date.valueOf(start.toLocalDate()));

        when(mockRs.getTime("start_time"))
            .thenReturn(java.sql.Time.valueOf(start.toLocalTime()));

        when(mockRs.getTime("end_time"))
            .thenReturn(java.sql.Time.valueOf(end.toLocalTime()));

        when(mockRs.getInt("max_capacity")).thenReturn(capacity);
        when(mockRs.getInt("booked_count")).thenReturn(booked);

        // optional
        when(mockInsertPs.executeUpdate()).thenReturn(1);
        when(mockUpdatePs.executeUpdate()).thenReturn(1);
    }

    /** ============================
     * SUCCESS
     * ============================
     */
    @Test
    public void testBookAppointment_Success() throws Exception {

        LocalDateTime start = LocalDateTime.now().plusHours(5); // 🔥 حل مشكلة الوقت
        LocalDateTime end = start.plusMinutes(60);

        setupMockSlot(start, end, 5, 2);

        assertDoesNotThrow(() ->
            appointmentService.bookAppointment(userId, slotId, 2, AppointmentType_y.STANDARD)
        );

        verify(mockInsertPs).executeUpdate();
        verify(mockUpdatePs).executeUpdate();
        verify(mockConn).commit();
    }

    /** ============================
     * OVER CAPACITY
     * ============================
     */
    @Test
    public void testBookAppointment_OverCapacity() throws Exception {

        LocalDateTime start = LocalDateTime.now().plusHours(5);
        LocalDateTime end = start.plusMinutes(60);

        setupMockSlot(start, end, 5, 4);

        SQLException ex = assertThrows(SQLException.class, () ->
            appointmentService.bookAppointment(userId, slotId, 2, AppointmentType_y.STANDARD)
        );

        assertEquals("Not enough capacity for this slot", ex.getMessage());
        verify(mockConn).rollback();
        verify(mockInsertPs, never()).executeUpdate(); // 🔥 مهم
    }

    /** ============================
     * PAST SLOT
     * ============================
     */
    @Test
    public void testBookAppointment_PastSlot() throws Exception {

        LocalDateTime start = LocalDateTime.now().minusHours(5);
        LocalDateTime end = start.plusMinutes(60);

        setupMockSlot(start, end, 5, 0);

        SQLException ex = assertThrows(SQLException.class, () ->
            appointmentService.bookAppointment(userId, slotId, 1, AppointmentType_y.STANDARD)
        );

        assertEquals("Cannot book a past slot", ex.getMessage());
        verify(mockConn).rollback();
    }

    /** ============================
     * SHORT DURATION
     * ============================
     */
    @Test
    public void testBookAppointment_ShortDuration() throws Exception {

        LocalDateTime start = LocalDateTime.now().plusHours(5);
        LocalDateTime end = start.plusMinutes(20);

        setupMockSlot(start, end, 5, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            appointmentService.bookAppointment(userId, slotId, 1, AppointmentType_y.STANDARD)
        );

        assertTrue(ex.getMessage().contains("Duration"));
        verify(mockConn).rollback();
    }

    /** ============================
     * SLOT NOT FOUND
     * ============================
     */
    @Test
    public void testBookAppointment_SlotNotFound() throws Exception {

        when(mockConn.prepareStatement(startsWith("SELECT"))).thenReturn(mockSelectPs);
        when(mockSelectPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        SQLException ex = assertThrows(SQLException.class, () ->
            appointmentService.bookAppointment(userId, slotId, 1, AppointmentType_y.STANDARD)
        );

        assertEquals("Slot not found", ex.getMessage());
        verify(mockConn).rollback();
    }

    /** ============================
     * getUserAppointments
     * ============================
     */
    @Test
    public void testGetUserAppointments() throws Exception {

        List<Appointment> mockList = Arrays.asList(
            mock(Appointment.class),
            mock(Appointment.class)
        );

        when(mockScheduleRepo.getAppointments(userId)).thenReturn(mockList);

        List<Appointment> result = appointmentService.getUserAppointments(userId);

        assertEquals(2, result.size());
        verify(mockScheduleRepo).getAppointments(userId);
    }

    /** ============================
     * getAvailableSlots
     * ============================
     */
    @Test
    public void testGetAvailableSlots() {

        List<AppointmentSlot_y> mockSlots = Arrays.asList(
            mock(AppointmentSlot_y.class),
            mock(AppointmentSlot_y.class)
        );

        when(mockSlotService.getAvailableSlots()).thenReturn(mockSlots);

        List<AppointmentSlot_y> result = appointmentService.getAvailableSlots();

        assertEquals(2, result.size());
        verify(mockSlotService).getAvailableSlots();
    }

    /** ============================
     * addSlot
     * ============================
     */
    @Test
    public void testAddSlot() {

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);

        appointmentService.addSlot(start, end, 5, 99);

        verify(mockSlotService).addSlot(
            start.toLocalDate(),
            start.toLocalTime(),
            end.toLocalTime(),
            5,
            99
        );
    }
}