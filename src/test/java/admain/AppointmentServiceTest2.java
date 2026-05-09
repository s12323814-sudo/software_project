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

    @Mock SlotService_y slotService;
    @Mock scheduleRepository repo;

    private Connection conn;
    private PreparedStatement psSelect;
    private PreparedStatement psInsert;
    private PreparedStatement psUpdate;
    private ResultSet rs;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        conn     = mock(Connection.class);
        psSelect = mock(PreparedStatement.class);
        psInsert = mock(PreparedStatement.class);
        psUpdate = mock(PreparedStatement.class);
        rs       = mock(ResultSet.class);

        when(conn.prepareStatement(contains("SELECT"))).thenReturn(psSelect);
        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(psInsert.executeUpdate()).thenReturn(1);
        when(psUpdate.executeUpdate()).thenReturn(1);
        doNothing().when(conn).setAutoCommit(anyBoolean());
        doNothing().when(conn).commit();
        doNothing().when(conn).rollback();
    }

    private AppointmentService svc() {
        return new AppointmentService(conn, slotService, repo);
    }

    // ===== setup slot مستقبلي ناجح =====
    private void setupValidSlot(int capacity, int booked, LocalTime start, LocalTime end) throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf(start));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf(end));
        when(rs.getInt("max_capacity")).thenReturn(capacity);
        when(rs.getInt("booked_count")).thenReturn(booked);
    }

    // ===== Constructor =====

    @Test
    void testDefaultConstructor() {
        assertNotNull(new AppointmentService());
    }

    @Test
    void testParamConstructor() {
        assertNotNull(svc());
    }

    // ===== getUserAppointments =====

    @Test
    void testGetUserAppointments_returnsData() throws Exception {
        when(repo.getAppointments(1)).thenReturn(List.of(new Appointment()));
        assertEquals(1, svc().getUserAppointments(1).size());
    }

    @Test
    void testGetUserAppointments_empty() throws Exception {
        when(repo.getAppointments(2)).thenReturn(Collections.emptyList());
        assertTrue(svc().getUserAppointments(2).isEmpty());
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
        LocalDateTime start = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(9, 0));
        LocalDateTime end   = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        svc().addSlot(start, end, 5, 1);
        verify(slotService).addSlot(start.toLocalDate(), start.toLocalTime(), end.toLocalTime(), 5, 1);
    }

    // ===== bookAppointment - success =====

    @Test
    void testBook_success() throws Exception {
        setupValidSlot(10, 0, LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertDoesNotThrow(() -> svc().bookAppointment(1, 1, 2, AppointmentType_y.ONLINE));
        verify(conn).commit();
    }

    // ===== bookAppointment - slot not found =====

    @Test
    void testBook_slotNotFound() throws Exception {
        when(rs.next()).thenReturn(false);
        assertThrows(SQLException.class, () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - null date =====

    @Test
    void testBook_nullDate() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(null);
        when(rs.getTime("start_time")).thenReturn(null);
        when(rs.getTime("end_time")).thenReturn(null);
        assertThrows(SQLException.class, () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - past slot =====

    @Test
    void testBook_pastSlot() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().minusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf(LocalTime.of(9, 0)));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf(LocalTime.of(10, 0)));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);
        assertThrows(SQLException.class, () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - not enough capacity =====

    @Test
    void testBook_notEnoughCapacity() throws Exception {
        setupValidSlot(2, 2, LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertThrows(SQLException.class, () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - duration too short =====

    @Test
    void testBook_durationTooShort() throws Exception {
        setupValidSlot(10, 0, LocalTime.of(9, 0), LocalTime.of(9, 20)); // 20 دقيقة
        assertThrows(IllegalArgumentException.class, () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - duration too long =====

    @Test
    void testBook_durationTooLong() throws Exception {
        setupValidSlot(10, 0, LocalTime.of(9, 0), LocalTime.of(11, 30)); // 150 دقيقة
        assertThrows(IllegalArgumentException.class, () -> svc().bookAppointment(1, 1, 1, AppointmentType_y.ONLINE));
        verify(conn).rollback();
    }

    // ===== bookAppointment - different types =====

    @Test
    void testBook_typeGeneral() throws Exception {
        setupValidSlot(10, 0, LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertDoesNotThrow(() -> svc().bookAppointment(1, 1, 1, AppointmentType_y.GENERAL));
        verify(conn).commit();
    }

    @Test
    void testBook_typeConsultation() throws Exception {
        setupValidSlot(10, 0, LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertDoesNotThrow(() -> svc().bookAppointment(1, 1, 1, AppointmentType_y.CONSULTATION));
        verify(conn).commit();
    }
}
