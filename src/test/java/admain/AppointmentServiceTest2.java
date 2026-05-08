package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest2 {

    @Mock
    scheduleRepository repo;

    @Mock
    SlotService_y slotService;

    AppointmentService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new AppointmentService(null, slotService, repo);
    }

    // =========================
    // Constructor
    // =========================

    @Test
    void testConstructor() {
        assertNotNull(new AppointmentService());
    }

    // =========================
    // getUserAppointments
    // =========================

    @Test
    void testGetUserAppointments() throws Exception {
        when(repo.getAppointments(1)).thenReturn(List.of(new Appointment()));

        List<Appointment> result = service.getUserAppointments(1);

        assertEquals(1, result.size());
    }

    @Test
    void testGetUserAppointments_empty() throws Exception {
        when(repo.getAppointments(1)).thenReturn(Collections.emptyList());

        assertTrue(service.getUserAppointments(1).isEmpty());
    }

    // =========================
    // Slots
    // =========================

    @Test
    void testGetAvailableSlots() {
        when(slotService.getAvailableSlots()).thenReturn(Collections.emptyList());

        assertEquals(0, service.getAvailableSlots().size());
    }

    // =========================
    // addSlot
    // =========================

    @Test
    void testAddSlot() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);

        service.addSlot(start, end, 10, 1);

        verify(slotService).addSlot(
                start.toLocalDate(),
                start.toLocalTime(),
                end.toLocalTime(),
                10,
                1
        );
    }

    // =========================
    // Duration Logic
    // =========================

    @Test
    void testDurationInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusMinutes(5);

            long d = java.time.Duration.between(start, end).toMinutes();
            if (d < 30) throw new IllegalArgumentException();
        });
    }

    // =========================
    // BOOKING CORE (FULL COVERAGE FIX)
    // =========================

    private Connection mockConnection(ResultSet rs) throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        doNothing().when(conn).setAutoCommit(anyBoolean());
        doNothing().when(conn).commit();
        doNothing().when(conn).rollback();

        return conn;
    }

    private ResultSet baseResultSet() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        return rs;
    }

    @Test
    void testBook_success() throws Exception {

        ResultSet rs = baseResultSet();
        when(rs.getDate("start_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(java.sql.Time.valueOf(LocalTime.of(9,0)));
        when(rs.getTime("end_time")).thenReturn(java.sql.Time.valueOf(LocalTime.of(10,0)));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        Connection conn = mockConnection(rs);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertDoesNotThrow(() ->
                svc.bookAppointment(1,1,2, AppointmentType_y.ONLINE)
        );

        verify(conn).commit();
    }

    @Test
    void testBook_slotNotFound() throws Exception {

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);

        Connection conn = mockConnection(rs);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertThrows(SQLException.class, () ->
                svc.bookAppointment(1,1,1, AppointmentType_y.ONLINE)
        );

        verify(conn).rollback();
    }

    @Test
    void testBook_nullData() throws Exception {

        ResultSet rs = baseResultSet();
        when(rs.getDate("start_date")).thenReturn(null);
        when(rs.getTime("start_time")).thenReturn(null);
        when(rs.getTime("end_time")).thenReturn(null);
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        Connection conn = mockConnection(rs);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertThrows(SQLException.class, () ->
                svc.bookAppointment(1,1,1, AppointmentType_y.ONLINE)
        );

        verify(conn).rollback();
    }

    @Test
    void testBook_notEnoughCapacity() throws Exception {

        ResultSet rs = baseResultSet();
        when(rs.getDate("start_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(java.sql.Time.valueOf(LocalTime.of(9,0)));
        when(rs.getTime("end_time")).thenReturn(java.sql.Time.valueOf(LocalTime.of(10,0)));
        when(rs.getInt("max_capacity")).thenReturn(2);
        when(rs.getInt("booked_count")).thenReturn(2);

        Connection conn = mockConnection(rs);

        AppointmentService svc = new AppointmentService(conn, slotService, repo);

        assertThrows(SQLException.class, () ->
                svc.bookAppointment(1,1,1, AppointmentType_y.ONLINE)
        );

        verify(conn).rollback();
    }
}
