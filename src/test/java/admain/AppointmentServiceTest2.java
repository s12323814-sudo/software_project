package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.time.*;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest2 {

    @Mock scheduleRepository repo;
    @Mock SlotService_y slotService;

    // connection جاهز للاستخدام في كل test
    private Connection conn;
    private PreparedStatement ps;
    private PreparedStatement psInsert;
    private PreparedStatement psUpdate;
    private ResultSet rs;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        conn     = mock(Connection.class);
        ps       = mock(PreparedStatement.class);
        psInsert = mock(PreparedStatement.class);
        psUpdate = mock(PreparedStatement.class);
        rs       = mock(ResultSet.class);

        // كل prepareStatement يرجع mock مختلف حسب الـ SQL
        when(conn.prepareStatement(contains("SELECT"))).thenReturn(ps);
        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        when(ps.executeQuery()).thenReturn(rs);
        when(psInsert.executeUpdate()).thenReturn(1);
        when(psUpdate.executeUpdate()).thenReturn(1);

        doNothing().when(conn).setAutoCommit(anyBoolean());
        doNothing().when(conn).commit();
        doNothing().when(conn).rollback();
    }

    // ===== Helper لإعداد ResultSet ناجح =====
    private void setupSuccessfulSlot() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date"))
            .thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
            .thenReturn(Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
            .thenReturn(Time.valueOf(LocalTime.of(10, 0)));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);
    }

    private AppointmentService svc() {
        return new AppointmentService(conn, slotService, repo);
    }

    // ===== Constructor =====
    @Test
    void testConstructor() {
        assertNotNull(new AppointmentService());
    }

    // ===== getUserAppointments =====
    @Test
    void testGetUserAppointments() throws Exception {
        when(repo.getAppointments(1)).thenReturn(List.of(new Appointment()));
        assertEquals(1, svc().getUserAppointments(1).size());
    }

    @Test
    void testGetUserAppointments_empty() throws Exception {
        when(repo.getAppointments(1)).thenReturn(Collections.emptyList());
        assertTrue(svc().getUserAppointments(1).isEmpty());
    }

    // ===== getAvailableSlots =====
    @Test
    void testGetAvailableSlots() {
        when(slotService.getAvailableSlots()).thenReturn(Collections.emptyList());
        assertEquals(0, svc().getAvailableSlots().size());
    }

    // ===== addSlot =====
    @Test
    void testAddSlot() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        svc().addSlot(start, end, 10, 1);
        verify(slotService).addSlot(
            start.toLocalDate(), start.toLocalTime(),
            end.toLocalTime(), 10, 1
        );
    }

    // ===== bookAppointment - success =====
    @Test
    void testBook_success() throws Exception {
        setupSuccessfulSlot();
        assertDoesNotThrow(() -> svc().bookAppointment(1, 1, 2, AppointmentType_y.ONLINE));
        verify(conn).commit();
    }

    // ===== bookAppointment - slot not found =====
    @Test
    void testBook_slotNotFound() throws Exception {
        when(rs.next()).thenReturn(false);
        assertThrows(SQLException.class,
            () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - null date/time =====
    @Test
    void testBook_nullDate() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(null);
        when(rs.getTime("start_time")).thenReturn(null);
        when(rs.getTime("end_time")).thenReturn(null);
        assertThrows(SQLException.class,
            () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - past slot =====
    @Test
    void testBook_pastSlot() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date"))
            .thenReturn(Date.valueOf(LocalDate.now().minusDays(1)));
        when(rs.getTime("start_time"))
            .thenReturn(Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
            .thenReturn(Time.valueOf(LocalTime.of(10, 0)));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);
        assertThrows(SQLException.class,
            () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - not enough capacity =====
    @Test
    void testBook_notEnoughCapacity() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date"))
            .thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
            .thenReturn(Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
            .thenReturn(Time.valueOf(LocalTime.of(10, 0)));
        when(rs.getInt("max_capacity")).thenReturn(2);
        when(rs.getInt("booked_count")).thenReturn(2);
        assertThrows(SQLException.class,
            () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - invalid duration too short =====
    @Test
    void testBook_durationTooShort() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date"))
            .thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
            .thenReturn(Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
            .thenReturn(Time.valueOf(LocalTime.of(9, 20))); // 20 دقيقة < 30
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);
        assertThrows(IllegalArgumentException.class,
            () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - invalid duration too long =====
    @Test
    void testBook_durationTooLong() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date"))
            .thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time"))
            .thenReturn(Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time"))
            .thenReturn(Time.valueOf(LocalTime.of(11, 30))); // 150 دقيقة > 120
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);
        assertThrows(IllegalArgumentException.class,
            () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }
}
