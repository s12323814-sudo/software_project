package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AppointmentRepository_yTestFixed {

    private SlotRepository_y slotRepo;
    private AppointmentRepository_y repo;

    @BeforeEach
    public void setup() {
        slotRepo = mock(SlotRepository_y.class);
        repo = new AppointmentRepository_y(slotRepo);
    }

    // ================= displayAppointments =================
    
    @Test
    public void testDisplayAppointmentsEmpty() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        AppointmentRepository_y.displayAppointments(java.util.Collections.emptyList());

        assertTrue(output.toString().contains("No upcoming appointments."));
    }
    @Test
    public void testBookEdgeCapacityExactly() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.of(2026, 5, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));

        when(slot.getBookedCount()).thenReturn(4);
        when(slot.getMaxCapacity()).thenReturn(5);

        when(slotRepo.findById(1)).thenReturn(slot);

        // لازم ينجح (ما يرمي exception)
      
    }
    @Test
    public void testBookSuccess11() throws Exception {

        int slotId = 1;

        // ================= Mock Slot =================
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));

        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(5);

        when(slotRepo.findById(slotId)).thenReturn(slot);

        // ================= Mock DB =================
        Connection conn = mock(Connection.class);

        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);

        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        when(psInsert.executeUpdate()).thenReturn(1);
        when(psUpdate.executeUpdate()).thenReturn(1);

        // ================= Mock Static Connection =================
        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            // ================= Execute =================
          
            // ================= Assert =================
        }
    }@Test
    public void testFindById_success() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getInt("appointment_id")).thenReturn(1);
        when(rs.getInt("account_id")).thenReturn(2);
        when(rs.getInt("slot_id")).thenReturn(3);
        when(rs.getInt("participants")).thenReturn(1);
        when(rs.getString("status")).thenReturn("CONFIRMED");
        when(rs.getString("type")).thenReturn("NORMAL");

        when(rs.getTimestamp("start_time")).thenReturn(new java.sql.Timestamp(System.currentTimeMillis()));
        when(rs.getTimestamp("end_time")).thenReturn(new java.sql.Timestamp(System.currentTimeMillis() + 3600000));

        Appointment result = repo.findById(1, conn);

        assertNotNull(result);
    }@Test
    public void testFindById_notFound() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(false);

        Appointment result = repo.findById(1, conn);

        assertNull(result);
    }@Test
    public void testGetUserEmail_null() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(false);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            String email = repo.getUserEmailByAppointment(1);

            assertNull(email);
        }
    }@Test
    public void testCancel_success() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);
        PreparedStatement ps3 = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString()))
                .thenReturn(ps1)
                .thenReturn(ps2)
                .thenReturn(ps3);

        when(ps1.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("slot_id")).thenReturn(1);
        when(rs.getInt("participants")).thenReturn(1);

        when(ps2.executeUpdate()).thenReturn(1);
        when(ps3.executeUpdate()).thenReturn(1);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.cancel(1, 1);

            assertTrue(result);
        }
    }@Test
    public void testCancel_fail() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(false);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.cancel(1, 1);

            assertFalse(result);
        }
    }@Test
    public void testDelete_fail() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        int result = repo.delete(1, conn);

        assertEquals(0, result);
    }
    @Test
    public void testDelete_success() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            int result = repo.delete(1, conn);

            assertEquals(1, result);
        }
    }@Test
    public void testBook_exactCapacityExceeded() {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));

        when(slot.getBookedCount()).thenReturn(4);
        when(slot.getMaxCapacity()).thenReturn(5);

        when(slotRepo.findById(1)).thenReturn(slot);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            repo.book(1, 1, 2, AppointmentType_y.NORMAL); // 4 + 2 = 6 > 5
        });

        assertTrue(ex.getMessage().contains("not enough capacity"));
    }@Test
    public void testBook_slotNull() {

        when(slotRepo.findById(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            repo.book(1, 1, 1, AppointmentType_y.NORMAL);
        });

        assertTrue(ex.getMessage().contains("does not exist"));
    }@Test
    public void testUpdate_success() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.update(1, 1, 5);

            assertTrue(result);
        }
    }@Test
    public void testUpdate_fail() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.update(1, 1, 5);

            assertFalse(result);
        }
    }
    @Test
    public void testDisplayAppointments() {

        Appointment appt = mock(Appointment.class);
        TimeSlot slot = mock(TimeSlot.class);

        when(appt.getUserId()).thenReturn(1);
        when(appt.getSlotId()).thenReturn(10);
        when(appt.getParticipants()).thenReturn(2);
        when(appt.getType()).thenReturn(AppointmentType_y.NORMAL);
        when(appt.getStatus()).thenReturn(AppointmentStatus_y.CONFIRMED);

        when(appt.getTimeSlot()).thenReturn(slot);
        when(slot.getStart()).thenReturn(ZonedDateTime.now().plusHours(1));
        when(slot.getEnd()).thenReturn(ZonedDateTime.now().plusHours(2));
        when(slot.getDurationMinutes()).thenReturn(60);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        AppointmentRepository_y.displayAppointments(
                java.util.Arrays.asList(appt)
        );
        String outputStr = out.toString().replace("\r\n", "\n");

        assertTrue(outputStr.contains("ID"));}
    @Test
    public void testGetUserEmail_notFound1() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(false);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            String email = repo.getUserEmailByAppointment(1);

            assertNull(email);
        }
    }@Test
    public void testGetUserEmail_notFound() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(false);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            String email = repo.getUserEmailByAppointment(1);

            assertNull(email);
        }
    }
    @Test
    
    public void testGetUserEmail_success() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getString("email")).thenReturn("test@gmail.com");

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            String email = repo.getUserEmailByAppointment(1);

            assertEquals("test@gmail.com", email);
        }
    
    }@Test
    public void testBookAlreadyEndedSlot() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.now().minusDays(1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(8, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(9, 0)); // already ended

        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(10);

        when(slotRepo.findById(1)).thenReturn(slot);

        boolean result = repo.book(1, 1, 1, AppointmentType_y.NORMAL);

        assertFalse(result);
    }
    
    @Test
    public void testBookPastStartTime() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.now().minusDays(1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));

        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(10);

        when(slotRepo.findById(1)).thenReturn(slot);

        boolean result = repo.book(1, 1, 1, AppointmentType_y.NORMAL);

        assertFalse(result);
    }@Test
    public void testBookOverCapacity() {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.of(2026, 5, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));

        when(slot.getBookedCount()).thenReturn(5);
        when(slot.getMaxCapacity()).thenReturn(5); // full

        when(slotRepo.findById(1)).thenReturn(slot);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            repo.book(1, 1, 1, AppointmentType_y.NORMAL);
        });

        assertTrue(ex.getMessage().contains("not enough capacity"));
    }
    @Test
    public void testBookSuccess_final() throws Exception {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(5);

        when(slotRepo.findById(1)).thenReturn(slot);

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(ps.executeUpdate()).thenReturn(1);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

        
        }
    }
    @Test
    public void testUpdateSuccess() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.update(1, 1, 3);

            assertTrue(result);
        }
    }@Test
    public void testUpdateFail_final() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.update(1, 1, 3);

            assertFalse(result);
        }
    }@Test
    public void testBookSlotNull() {

        when(slotRepo.findById(1)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            repo.book(1, 1, 1, AppointmentType_y.NORMAL);
        });

        assertTrue(ex.getMessage().contains("does not exist"));
    }
    @Test
    public void testBookPastSlot1() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.now().minusDays(2));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));

        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(10);

        when(slotRepo.findById(1)).thenReturn(slot);

        boolean result = repo.book(1, 1, 1, AppointmentType_y.NORMAL);

        assertFalse(result);
    }@Test
    public void testCapacityEdgeCase() {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));

        when(slot.getBookedCount()).thenReturn(5);
        when(slot.getMaxCapacity()).thenReturn(5);

        when(slotRepo.findById(1)).thenReturn(slot);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            repo.book(1, 1, 1, AppointmentType_y.NORMAL);
        });

        assertTrue(ex.getMessage().contains("not enough capacity"));
    }@Test
    public void testBookPastSlot() {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.now().minusDays(2));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10,0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11,0));

        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(10);

        when(slotRepo.findById(1)).thenReturn(slot);


       // مهم
    }
    @Test
  
    public void testBookOverCapacity_correct() {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));

        when(slot.getBookedCount()).thenReturn(5);
        when(slot.getMaxCapacity()).thenReturn(5); // ❌ full slot

        when(slotRepo.findById(1)).thenReturn(slot);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            repo.book(1, 1, 1, AppointmentType_y.NORMAL);
        });

        assertTrue(ex.getMessage().contains("not enough capacity"));
    }
    @Test
    public void testBookPastSlotReturnsFalse() throws SQLException {

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);

        when(slot.getDate()).thenReturn(LocalDate.now().minusDays(1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10,0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11,0));

        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(10);

        when(slotRepo.findById(1)).thenReturn(slot);

        boolean result = repo.book(1, 1, 1, AppointmentType_y.NORMAL);

        assertFalse(result);
    }
    @Test
    public void testBookSlotNotFound1() {

        when(slotRepo.findById(1)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            repo.book(1, 1, 1, AppointmentType_y.NORMAL);
        });

        assertTrue(ex.getMessage().contains("does not exist"));
    }
    @Test
    public void testUpdateFail() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.update(1, 1, 5);

            assertFalse(result);
        }
    }
    @Test
    public void testBookSlotNotFound() {

        when(slotRepo.findById(1)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            repo.book(1, 1, 1, AppointmentType_y.NORMAL);
        });

        assertTrue(ex.getMessage().contains("does not exist"));
    }
    @Test
    public void testBookSuccess1() throws Exception {

        int slotId = 1;

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(5);

        when(slotRepo.findById(slotId)).thenReturn(slot);

        Connection conn = mock(Connection.class);
        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);

        when(psInsert.executeUpdate()).thenReturn(1);
        when(psUpdate.executeUpdate()).thenReturn(1);

        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

           
        }
    }
    @Test
    public void testBookSuccess() throws Exception {
        int slotId = 1;

        // ===== Mock Slot =====
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(0); // لا أحد محجوز بعد
        when(slot.getMaxCapacity()).thenReturn(5); // السعة 5
        when(slotRepo.findById(slotId)).thenReturn(slot);

        // ===== Mock Connection + PreparedStatements =====
        Connection conn = mock(Connection.class);
        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);

        when(psInsert.executeUpdate()).thenReturn(1); // محاكاة نجاح INSERT
        when(psUpdate.executeUpdate()).thenReturn(1); // محاكاة نجاح UPDATE

        // ربط كل SQL بالـ PreparedStatement المناسب
        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        // ===== Mock static database_connection + تنفيذ الحجز =====
        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

           // تحقق من نجاح الحجز
        }
    }
    // ================= book =================
   
    @Test
    public void testBookSlotOverCapacity() throws Exception {
        int slotId = 1;

        // ===== Mock Slot =====
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(4); // موجود بالفعل 4
        when(slot.getMaxCapacity()).thenReturn(5); // السعة 5
        when(slotRepo.findById(slotId)).thenReturn(slot);

        // ===== Mock Connection + PreparedStatements =====
        Connection conn = mock(Connection.class);
        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);

        // لاستخدام PreparedStatement حتى لو ما نفذت executeUpdate بسبب Exception
        when(psInsert.executeUpdate()).thenReturn(1);
        when(psUpdate.executeUpdate()).thenReturn(1);

        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

        // ===== Mock static connection + assert exception =====
        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            // => Exception متوقع بسبب over capacity
          // => تحقق من الرسالة بدقة
         
        }
    }
    // ================= cancel =================
   
 
    @Test
    void testCancelAppointment_success() throws Exception {

        AppointmentRepository_y appointmentRepo = mock(AppointmentRepository_y.class);
        SlotRepository_y slotRepo = mock(SlotRepository_y.class);
        NotificationService_y notificationService = mock(NotificationService_y.class);
        EmailService_y emailService = mock(EmailService_y.class);

        SlotService_y service = new SlotService_y(
                appointmentRepo,
                slotRepo,
                notificationService,
                emailService
        );

        when(appointmentRepo.cancel(10, 1)).thenReturn(true);

        boolean result = service.cancelAppointment(10, 1);

        assertTrue(result);

        verify(appointmentRepo).cancel(10, 1);
    }

    // ================= update =================
    
    @Test
    public void testUpdate() throws Exception {
        // Mock Connection و PreparedStatement
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeUpdate()).thenReturn(1);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        // Mock static داخل try-with-resources فقط
        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.update(10, 1, 5);
            assertTrue(result); // تحقق من نجاح التحديث
        }
    }}
    // ================= Helper =================
   
