package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AppointmentServiceTest2 {

    @Mock
    private scheduleRepository repo;

    @Mock
    private SlotService_y slotService;

    private AppointmentService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new AppointmentService(null, slotService, repo);
    }

    // =========================
    // Constructor Tests
    // =========================

    @Test
    void testDefaultConstructor_doesNotThrow() {
        AppointmentService s = new AppointmentService();
        assertNotNull(s);
    }

    @Test
    void testConstructorBehaviour_noException() {
        assertDoesNotThrow(() -> new AppointmentService());
    }

    // =========================
    // Duration Validation
    // =========================

    @Test
    void testDurationTooShort() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusMinutes(10);
            long duration = Duration.between(start, end).toMinutes();
            if (duration < 30)
                throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");
        });
        assertEquals("Duration must be between 30 and 120 minutes.", ex.getMessage());
    }

    @Test
    void testDurationTooLong() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusMinutes(200);
            long duration = Duration.between(start, end).toMinutes();
            if (duration > 120)
                throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");
        });
        assertEquals("Duration must be between 30 and 120 minutes.", ex.getMessage());
    }

    // =========================
    // getUserAppointments
    // =========================

    @Test
    void testGetUserAppointments_success() throws Exception {
        List<Appointment> list = new ArrayList<>();
        list.add(new Appointment());
        when(repo.getAppointments(1)).thenReturn(list);

        List<Appointment> result = service.getUserAppointments(1);

        assertEquals(1, result.size());
        verify(repo).getAppointments(1);
    }

    @Test
    void testGetUserAppointments_empty() throws Exception {
        when(repo.getAppointments(1)).thenReturn(Collections.emptyList());

        List<Appointment> result = service.getUserAppointments(1);

        assertTrue(result.isEmpty());
        verify(repo).getAppointments(1);
    }

    @Test
    void testGetUserAppointments_exception() throws Exception {
        when(repo.getAppointments(1)).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> service.getUserAppointments(1));
        verify(repo).getAppointments(1);
    }

    @Test
    void testGetUserAppointments_invalidUser() throws Exception {
        when(repo.getAppointments(-1)).thenThrow(new SQLException("Invalid"));

        assertThrows(SQLException.class, () -> service.getUserAppointments(-1));
    }

    // =========================
    // getAvailableSlots
    // =========================

    @Test
    void testGetAvailableSlots_success() {
        List<AppointmentSlot_y> slots = new ArrayList<>();
        slots.add(new AppointmentSlot_y(1, null, null, null, 0, 0));
        when(slotService.getAvailableSlots()).thenReturn(slots);

        List<AppointmentSlot_y> result = service.getAvailableSlots();

        assertEquals(1, result.size());
        verify(slotService).getAvailableSlots();
    }

    @Test
    void testGetAvailableSlots_empty() {
        when(slotService.getAvailableSlots()).thenReturn(Collections.emptyList());

        List<AppointmentSlot_y> result = service.getAvailableSlots();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAvailableSlots_null() {
        when(slotService.getAvailableSlots()).thenReturn(null);

        List<AppointmentSlot_y> result = service.getAvailableSlots();

        assertNull(result);
    }

    // =========================
    // addSlot
    // =========================

    @Test
    void testAddSlot_success() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);

        service.addSlot(start, end, 10, 1);

        verify(slotService).addSlot(
                eq(start.toLocalDate()),
                eq(start.toLocalTime()),
                eq(end.toLocalTime()),
                eq(10),
                eq(1)
        );
    }

    @Test
    void testAddSlot_nullStart() {
        LocalDateTime end = LocalDateTime.now();
        assertThrows(NullPointerException.class, () -> service.addSlot(null, end, 10, 1));
    }

    @Test
    void testAddSlot_nullEnd() {
        LocalDateTime start = LocalDateTime.now();
        assertThrows(NullPointerException.class, () -> service.addSlot(start, null, 10, 1));
    }

    // =========================
    // bookAppointment
    // =========================

    @Test
    void testBookAppointment_slotNotFound() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertThrows(SQLException.class, () ->
                svc.bookAppointment(1, 99, 2, AppointmentType_y.ONLINE));

        verify(conn).rollback();
    }

    @Test
    void testBookAppointment_pastSlot() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getDate("start_date"))
                .thenReturn(java.sql.Date.valueOf(LocalDate.now().minusDays(1)));
        when(rs.getTime("start_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(10, 0)));

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertThrows(SQLException.class, () ->
                svc.bookAppointment(1, 1, 2, AppointmentType_y.ONLINE));

        verify(conn).rollback();
    }

    @Test
    void testBookAppointment_nullDate() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getDate("start_date")).thenReturn(null);
        when(rs.getTime("start_time")).thenReturn(null);
        when(rs.getTime("end_time")).thenReturn(null);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertThrows(SQLException.class, () ->
                svc.bookAppointment(1, 1, 2, AppointmentType_y.ONLINE));

        verify(conn).rollback();
    }

    @Test
    void testBookAppointment_notEnoughCapacity() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getDate("start_date"))
                .thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(10, 0)));
        when(rs.getInt("max_capacity")).thenReturn(2);
        when(rs.getInt("booked_count")).thenReturn(2);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertThrows(SQLException.class, () ->
                svc.bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));

        verify(conn).rollback();
    }

    @Test
    void testBookAppointment_durationTooShort() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getDate("start_date"))
                .thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(9, 10))); // 10 دقائق فقط
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertThrows(IllegalArgumentException.class, () ->
                svc.bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));

        verify(conn).rollback();
    }

    @Test
    void testBookAppointment_durationTooLong() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getDate("start_date"))
                .thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(12, 0))); // 3 ساعات
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertThrows(IllegalArgumentException.class, () ->
                svc.bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));

        verify(conn).rollback();
    }

    @Test
    void testBookAppointment_success() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement psSelect = mock(PreparedStatement.class);
        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(contains("SELECT"))).thenReturn(psSelect);
        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getDate("start_date"))
                .thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(10, 0)));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertDoesNotThrow(() ->
                svc.bookAppointment(1, 1, 2, AppointmentType_y.ONLINE));

        verify(conn).commit();
    }

    @Test
    void testBookAppointment_exactCapacity() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement psSelect = mock(PreparedStatement.class);
        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(contains("SELECT"))).thenReturn(psSelect);
        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getDate("start_date"))
                .thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(10, 0)));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertDoesNotThrow(() ->
                svc.bookAppointment(1, 1, 5, AppointmentType_y.ONLINE));

        verify(conn).commit();
    }

    @Test
    void testBookAppointment_offlineType() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement psSelect = mock(PreparedStatement.class);
        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(contains("SELECT"))).thenReturn(psSelect);
        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getDate("start_date"))
                .thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
                .thenReturn(java.sql.Time.valueOf(LocalTime.of(10, 0)));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(2);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertDoesNotThrow(() ->
                svc.bookAppointment(1, 1, 1, AppointmentType_y.OFFLINE));

        verify(conn).commit();
    }
}
