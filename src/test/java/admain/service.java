package admain;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import java.time.LocalDate;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class service {

    @Mock
    private Connection conn;

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    private AppointmentService service;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        service = new AppointmentService(conn, null, new scheduleRepository());

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
    }

    @Test
    void testBookAppointment_success() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf(LocalTime.of(10, 0)));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf(LocalTime.of(11, 0)));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(2);

        service.bookAppointment(1, 1, 2, AppointmentType_y.NORMAL);

        verify(conn).commit(); // تأكد إنه عمل commit
        verify(ps, atLeastOnce()).executeUpdate();
    }

    @Test
    void testBookAppointment_slotNotFound() throws Exception {

        when(rs.next()).thenReturn(false);

        assertThrows(SQLException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.NORMAL);
        });

        verify(conn).rollback();
    }

    @Test
    void testBookAppointment_capacityExceeded() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(5);

        assertThrows(SQLException.class, () -> {
            service.bookAppointment(1, 1, 2, AppointmentType_y.NORMAL);
        });

        verify(conn).rollback();
    }
    @Test
    void testBookAppointment_nullDate() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(null); // ❌

        assertThrows(SQLException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.NORMAL);
        });

        verify(conn).rollback();
    }@Test
    void testBookAppointment_pastSlot() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().minusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));

        assertThrows(SQLException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.NORMAL);
        });

        verify(conn).rollback();
    }@Test
    void testBookAppointment_zeroParticipants() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        service.bookAppointment(1, 1, 0, AppointmentType_y.NORMAL);

        verify(conn).commit(); // حالياً الكود بيسمحها
    
    }
    @Test
    void testBookAppointment_minDuration() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("10:30:00")); // 30 دقيقة
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        service.bookAppointment(1, 1, 1, AppointmentType_y.NORMAL);

        verify(conn).commit();
    }@Test
    void testBookAppointment_exceedMaxDuration() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("13:00:00")); // 180 دقيقة

        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.NORMAL);
        });

        verify(conn).rollback();
    }@Test
    void testBookAppointment_insertFails() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        // خليه يرمي exception وقت insert
        doThrow(new SQLException()).when(ps).executeUpdate();

        assertThrows(SQLException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.NORMAL);
        });

        verify(conn).rollback();
    }
    @Test
    void testBookAppointment_negativeParticipants() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        service.bookAppointment(1, 1, -2, AppointmentType_y.NORMAL);

        verify(conn).commit(); // ❌ Bug منطقي
    }
    @Test
    void testBookAppointment_invalidDuration() throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("10:10:00")); // 10 دقائق فقط

        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> {
            service.bookAppointment(1, 1, 1, AppointmentType_y.NORMAL);
        });

        verify(conn).rollback();
    }
}
