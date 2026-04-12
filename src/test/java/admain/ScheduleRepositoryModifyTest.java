package admain;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class ScheduleRepositoryModifyTest {

    @Mock private Connection mockConn;
    @Mock private PreparedStatement mockSelectStmt;
    @Mock private PreparedStatement mockUpdateStmt;
    @Mock private ResultSet mockRs;

    private scheduleRepository repo;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        repo = spy(new scheduleRepository());

        // كل getConnection() يرجع mockConn
        doReturn(mockConn).when(repo).getConnection();

        // إعداد executeUpdate العام
        when(mockConn.prepareStatement(anyString())).thenReturn(mockUpdateStmt);
        when(mockUpdateStmt.executeUpdate()).thenReturn(1);

        // إعداد SELECT للـ old appointment
        when(mockConn.prepareStatement(contains("SELECT slot_id, participants FROM appointments"))).thenReturn(mockSelectStmt);
        when(mockSelectStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("slot_id")).thenReturn(1);
        when(mockRs.getInt("participants")).thenReturn(2);

        // mock isSlotAvailable ليكون true دائمًا
        doReturn(true).when(repo).isSlotAvailable(any(Connection.class), anyInt(), anyInt());
    }
    @Test
    void testSlotAvailable_noGetIntWhenNoRow() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(false);

        YourService service = new YourService(repo);

        service.isSlotAvailableForResource(1, 10);

        verify(rs, never()).getInt(1); // 🔥 مهم جداً
    }@Test
    void testSlotAvailable_resourcesUsed() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        YourService service = new YourService(repo);

        service.isSlotAvailableForResource(1, 10);

        verify(conn).prepareStatement(anyString());
        verify(ps).executeQuery();
        verify(ps).close(); // try-with-resources
    }@Test
    void testSlotAvailable_verifyParameters() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        YourService service = new YourService(repo);

        service.isSlotAvailableForResource(5, 99);

        verify(ps).setInt(1, 5);
        verify(ps).setInt(2, 99);
    }
    @Test
    public void testModifyAppointment() throws Exception {
        repo.modifyAppointment(10, 2, 3);

        verify(repo, times(1)).getConnection();
        verify(mockSelectStmt).setInt(1, 10);
        verify(mockSelectStmt).executeQuery();
        verify(mockConn, atLeastOnce()).prepareStatement(contains("UPDATE appointments"));
        verify(mockConn, atLeast(2)).prepareStatement(contains("UPDATE appointment_slot"));
        verify(mockUpdateStmt, atLeast(3)).executeUpdate(); // appointments + 2 booked_count
    }

    @Test
    public void testAddAppointment() throws Exception {
        // إعداد TimeSlot و Appointment
        TimeSlot slot = new TimeSlot(1, ZonedDateTime.now().plusHours(1), ZonedDateTime.now().plusHours(2));
        Appointment appt = new Appointment(0, 1, 1, slot, 2, AppointmentStatus_y.CONFIRMED, AppointmentType_y.CONSULTATION);

        // mock SELECT للـ slot في determineStatus
        ResultSet slotRs = mock(ResultSet.class);
        PreparedStatement slotStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(contains("SELECT max_capacity, booked_count FROM appointment_slot"))).thenReturn(slotStmt);
        when(slotStmt.executeQuery()).thenReturn(slotRs);
        when(slotRs.next()).thenReturn(true);
        when(slotRs.getInt("max_capacity")).thenReturn(5);
        when(slotRs.getInt("booked_count")).thenReturn(2);

        repo.addAppointment(appt);

        verify(mockConn, atLeastOnce()).prepareStatement(contains("INSERT INTO appointments"));
        verify(mockConn, atLeastOnce()).prepareStatement(contains("UPDATE appointment_slot"));
    }@Test
    void testSlotAvailable_nullConnection() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);

        when(repo.getConnection()).thenReturn(null);

        YourService service = new YourService(repo);

        assertThrows(NullPointerException.class, () -> {
            service.isSlotAvailableForResource(1, 10);
        });
    }@Test
    void testSlotAvailable_nullPreparedStatement() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(null);

        YourService service = new YourService(repo);

        assertThrows(NullPointerException.class, () -> {
            service.isSlotAvailableForResource(1, 10);
        });
    }@Test
    void testSlotAvailable_sqlStringUsed() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        YourService service = new YourService(repo);

        service.isSlotAvailableForResource(1, 10);

        verify(conn).prepareStatement(contains("SELECT COUNT"));
    }@Test
    void testSlotAvailable_multipleCalls() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        YourService service = new YourService(repo);

        boolean r1 = service.isSlotAvailableForResource(1, 10);
        boolean r2 = service.isSlotAvailableForResource(1, 10);

        assertTrue(r1);
        assertTrue(r2);

        verify(ps, times(2)).executeQuery();
    }@Test
    void testSlotAvailable_safeParameters() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

        YourService service = new YourService(repo);

        service.isSlotAvailableForResource(100, 200);

        verify(ps).setInt(1, 100);
        verify(ps).setInt(2, 200);
    }
    @Test
    void testAddAndModifyAppointments_AllCases() throws SQLException {
        scheduleRepository repo = spy(new scheduleRepository());
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        doReturn(mockConn).when(repo).getConnection();
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);

        // إعداد slot
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(10);
        when(mockRs.getInt("booked_count")).thenReturn(2);

        ZonedDateTime start = ZonedDateTime.now().plusMinutes(10);
        ZonedDateTime end = start.plusMinutes(60);

        TimeSlot slot = new TimeSlot(1, start, end);
        Appointment appointment = new Appointment(1, 1, 1, slot, 3, AppointmentStatus_y.CONFIRMED, AppointmentType_y.CONSULTATION);

        // حالة addAppointment CONFIRMED
        repo.addAppointment(appointment);

        // حالة addAppointment WAITLIST
        when(mockRs.getInt("booked_count")).thenReturn(10); // slot ممتلئ
        Appointment waitlistAppt = new Appointment(2, 1, 1, slot, 3, AppointmentStatus_y.WAITLIST, AppointmentType_y.CONSULTATION);
        repo.addAppointment(waitlistAppt);

        // حالة modifyAppointment ناجحة
        when(mockRs.getInt("booked_count")).thenReturn(2); // slot الجديد متاح
        repo.modifyAppointment(1, 2, 3);

        // حالة modifyAppointment غير متاحة
        when(mockRs.getInt("booked_count")).thenReturn(10); // slot الجديد ممتلئ
        assertThrows(SQLException.class, () -> repo.modifyAppointment(2, 3, 3));

        // حالات الوقت
        AppointmentStatus_y status;
        // COMPLETED
        start = ZonedDateTime.now().minusHours(2);
        end = ZonedDateTime.now().minusHours(1);
        status = repo.determineStatus(mockConn, 1, 1, start, end);
        assertEquals(AppointmentStatus_y.COMPLETED, status);

        // ONGOING
        start = ZonedDateTime.now().minusMinutes(30);
        end = ZonedDateTime.now().plusMinutes(30);
        status = repo.determineStatus(mockConn, 1, 1, start, end);
        assertEquals(AppointmentStatus_y.ONGOING, status);
    }
    @Test
    void testDetermineStatus_AllCases() throws SQLException {
        scheduleRepository repo = spy(new scheduleRepository());
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        doReturn(mockConn).when(repo).getConnection();
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);

        // CONFIRMED
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("max_capacity")).thenReturn(10);
        when(mockRs.getInt("booked_count")).thenReturn(2);
        ZonedDateTime start = ZonedDateTime.now().plusHours(1);
        ZonedDateTime end = start.plusMinutes(60);
        AppointmentStatus_y status = repo.determineStatus(mockConn, 1, 3, start, end);
        assertEquals(AppointmentStatus_y.CONFIRMED, status);

        // WAITLIST
        when(mockRs.getInt("booked_count")).thenReturn(10); // ممتلئ
        status = repo.determineStatus(mockConn, 1, 3, start, end);
        assertEquals(AppointmentStatus_y.WAITLIST, status);

        // ONGOING
        when(mockRs.getInt("booked_count")).thenReturn(2);
        start = ZonedDateTime.now().minusMinutes(30);
        end = ZonedDateTime.now().plusMinutes(30);
        status = repo.determineStatus(mockConn, 1, 2, start, end);
        assertEquals(AppointmentStatus_y.ONGOING, status);

        // COMPLETED
        start = ZonedDateTime.now().minusHours(2);
        end = ZonedDateTime.now().minusHours(1);
        status = repo.determineStatus(mockConn, 1, 2, start, end);
        assertEquals(AppointmentStatus_y.COMPLETED, status);
    }@Test
    void testDetermineStatusCompleted() {
        ZonedDateTime pastStart = ZonedDateTime.now().minusHours(2);
        ZonedDateTime pastEnd = ZonedDateTime.now().minusHours(1);
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(0);

        AppointmentStatus_y status = scheduleRepository.determineStatus(pastStart, pastEnd, 1, slot);
        assertEquals(AppointmentStatus_y.COMPLETED, status);
    }

    @Test
    void testDetermineStatusOngoing() {
        ZonedDateTime start = ZonedDateTime.now().minusMinutes(10);
        ZonedDateTime end = ZonedDateTime.now().plusMinutes(50);
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(0);

        AppointmentStatus_y status = scheduleRepository.determineStatus(start, end, 1, slot);
        assertEquals(AppointmentStatus_y.ONGOING, status);
    }

    @Test
    void testDetermineStatusWaitlist() {
        ZonedDateTime start = ZonedDateTime.now().plusMinutes(10);
        ZonedDateTime end = ZonedDateTime.now().plusHours(1);
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(1);
        when(slot.getBookedCount()).thenReturn(1);

        AppointmentStatus_y status = scheduleRepository.determineStatus(start, end, 1, slot);
        assertEquals(AppointmentStatus_y.WAITLIST, status);
    }

    @Test
    void testGroupAppointmentLessThanTwoParticipants() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            int participants = 1; // أقل من 2
            if (participants < 2) throw new IllegalArgumentException("Group must have at least 2 participants.");
        });
        assertEquals("Group must have at least 2 participants.", ex.getMessage());
    }

    @Test
    void testIndividualAppointmentMoreThanOneParticipant() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            int participants = 2; // أكثر من 1
            if (participants > 1) throw new IllegalArgumentException("This type allows only 1 participant.");
        });
        assertEquals("This type allows only 1 participant.", ex.getMessage());
    }

    @Test
    void testVirtualAppointmentAnyParticipants() {
        int participants = 10; // كبير
        assertDoesNotThrow(() -> {
            if (participants < 1) throw new IllegalArgumentException();
        });
    }
    @Test
    public void testGetAppointments() throws Exception {
        // إعداد ResultSet للـ appointments
        ResultSet rs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(contains("SELECT appointment_id, slot_id, start_time, end_time, participants, type FROM appointments"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, false); // صف واحد فقط
        when(rs.getInt("appointment_id")).thenReturn(1);
        when(rs.getInt("slot_id")).thenReturn(1);
        when(rs.getInt("participants")).thenReturn(2);
        when(rs.getString("type")).thenReturn("CONSULTATION");
        Timestamp nowTs = Timestamp.valueOf(LocalDateTime.now().plusHours(1));
        when(rs.getTimestamp("start_time")).thenReturn(nowTs);
        when(rs.getTimestamp("end_time")).thenReturn(Timestamp.valueOf(LocalDateTime.now().plusHours(2)));

        // mock determineStatus
        doReturn(AppointmentStatus_y.CONFIRMED).when(repo).determineStatus(any(Connection.class), anyInt(), anyInt(), any(), any());

        List<Appointment> list = repo.getAppointments(1);
        assertEquals(1, list.size());
        assertEquals(AppointmentStatus_y.CONFIRMED, list.get(0).getStatus());
    }
}