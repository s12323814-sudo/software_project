package admain;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class AppointmentServiceTest {

    private SlotService_y slotService;
    private scheduleRepository repo;

    private Connection conn;
    private PreparedStatement psSelect;
    private PreparedStatement psInsert;
    private PreparedStatement psUpdate;
    private ResultSet rs;

    private AppointmentService service;

    @BeforeEach
    public void setup() throws Exception {
        slotService = mock(SlotService_y.class);
        repo = mock(scheduleRepository.class);

        conn = mock(Connection.class);
        psSelect = mock(PreparedStatement.class);
        psInsert = mock(PreparedStatement.class);
        psUpdate = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        // Service uses testConnection to avoid DB
        service = new AppointmentService(conn, slotService, repo);
    }
 // ===================== مدة طويلة جداً =====================
    @Test
    public void testBookAppointment_durationTooLong1() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2099-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("13:00:00")); // 3 ساعات = 180 دقائق
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            service.bookAppointment(1, 1, 2, AppointmentType_y.ONLINE)
        );
        assertTrue(ex.getMessage().contains("Duration must be between 30 and 120 minutes"));
    }

    // ===================== عدد المشاركين صفر =====================
    @Test
    public void testBookAppointment_zeroParticipants1() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2099-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00")); // 60 دقائق
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }
 // ===================== Participants invalid (0) =====================
    @Test
    public void testBookAppointment_zeroParticipants() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== Participants invalid (negative) =====================
    @Test
    public void testBookAppointment_negativeParticipants() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== Duration too long =====================
    @Test
    public void testBookAppointment_durationTooLong2() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("13:00:00")); // 3 ساعات
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== URGENT type with 2 participants (invalid) =====================
    @Test
    public void testBookAppointment_urgentMultipleParticipants2() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }
 // ===================== Test booking with exact remaining capacity =====================
    @Test
    public void testBookAppointment_exactRemainingCapacity() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(3);

    }

    // ===================== Test multiple participants within capacity =====================
    @Test
    public void testBookAppointment_multipleParticipantsWithinCapacity7() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("09:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(10);
        when(rs.getInt("booked_count")).thenReturn(5);

    }

 

    // ===================== Test getUserAppointments returns empty =====================
   

    // ===================== Test bookAppointment throws for null type =====================
 // ===================== مدة طويلة جدا =====================
    @Test
    public void testBookAppointment_durationTooLong() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("13:00:00")); // 3 ساعات = 180 دقيقة
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

       }

    // ===================== عدد مشاركين أكثر من السعة =====================
    @Test
    public void testBookAppointment_participantsExceedCapacity() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(3);

       
    }

    // ===================== اختبار إضافة موعد جديد =====================
    @Test
    public void testAddSlot_success() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 0);

        service.addSlot(start, end, 5, 1);

        // تحقق أن slotService.addSlot استدعي مع القيم الصحيحة
        verify(slotService).addSlot(start.toLocalDate(), start.toLocalTime(), end.toLocalTime(), 5, 1);
    }

    // ===================== الحصول على مواعيد المستخدم =====================
    @Test
    public void testGetUserAppointments8() throws Exception {
        List<Appointment> mockAppointments = List.of(mock(Appointment.class));
        when(repo.getAppointments(1)).thenReturn(mockAppointments);

        List<Appointment> result = service.getUserAppointments(1);
        assertTrue(result.size() == 1);
    }

    // ===================== الحصول على المواعيد المتاحة =====================
    @Test
    public void testGetAvailableSlots() throws Exception {
        List<AppointmentSlot_y> mockSlots = List.of(mock(AppointmentSlot_y.class));
        when(slotService.getAvailableSlots()).thenReturn(mockSlots);

        List<AppointmentSlot_y> result = service.getAvailableSlots();
        assertTrue(result.size() == 1);
    }

    // ===================== محاولة حجز بدون اتصال =====================
    @Test
    public void testBookAppointment_noConnection() {
        AppointmentService svc = new AppointmentService();
        assertThrows(Exception.class, () ->
            svc.bookAppointment(1, 1, 1, AppointmentType_y.ONLINE)
        );
    }

    // ===================== حجز مع أقل من الحد الأدنى =====================
    @Test
    public void testBookAppointment_minParticipants() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("10:30:00")); // بالحد الأدنى
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== حجز مع الحد الأقصى =====================
    @Test
    public void testBookAppointment_maxParticipants5() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("12:00:00")); // بالحد الأعلى 120 دقيقة
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== Test booking slot with startTime null =====================
    @Test
    public void testBookAppointment_startTimeNull() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(null);
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));

      }

    // ===================== Test booking slot with endTime null =====================
    @Test
    public void testBookAppointment_endTimeNull() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(null);

        }
    // ===================== GROUP type with 1 participant (invalid) =====================
    @Test
    public void testBookAppointment_groupSingleParticipant() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== VIRTUAL type with many participants =====================
    @Test
    public void testBookAppointment_virtualMultipleParticipants2() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(100);
        when(rs.getInt("booked_count")).thenReturn(50);

    }

    // ===================== IN_PERSON full capacity =====================
    @Test
    public void testBookAppointment_inPersonFull2() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(5);

    }

    // ===================== FOLLOW_UP short duration =====================
    @Test
    public void testBookAppointment_followUpShortDuration2() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("10:30:00")); // الحد الأدنى
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== ASSESSMENT type valid =====================
    @Test
    public void testBookAppointment_assessmentValid2() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== GENERAL type max participants =====================
    @Test
    public void testBookAppointment_generalMaxParticipants2() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(2);

    }

    // ===================== Multiple bookings consecutively =====================
    @Test
    public void testBookAppointment_multipleSequential2() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== Check remaining capacity after booking =====================
    @Test
    public void testBookAppointment_checkRemainingCapacity() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(2);

    }

    // ===================== Test minimum duration exact 30 mins =====================
    @Test
    public void testBookAppointment_minimumDuration2() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("10:30:00")); 
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }

    // ===================== Test maximum duration exact 120 mins =====================
    @Test
    public void testBookAppointment_maximumDuration() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00")); 
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("12:00:00")); 
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }
    // ===================== حجز كامل السعة مباشرة =====================
    @Test
    public void testBookAppointment_fullCapacityExact() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2099-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

        // إعداد INSERT + UPDATE
        when(conn.prepareStatement(contains("INSERT INTO appointments"))).thenReturn(psInsert);
        when(psInsert.executeUpdate()).thenReturn(1);

        when(conn.prepareStatement(contains("UPDATE appointment_slot"))).thenReturn(psUpdate);
        when(psUpdate.executeUpdate()).thenReturn(1);

        // نحاول حجز كل السعة
        service.bookAppointment(1, 1, 5, AppointmentType_y.GROUP);
    }

    // ===================== حجز نوع URGENT =====================
    @Test
    public void testBookAppointment_urgentType() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2099-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("09:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("09:45:00")); // 45 دقائق
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

        when(conn.prepareStatement(contains("INSERT INTO appointments"))).thenReturn(psInsert);
        when(psInsert.executeUpdate()).thenReturn(1);

        when(conn.prepareStatement(contains("UPDATE appointment_slot"))).thenReturn(psUpdate);
        when(psUpdate.executeUpdate()).thenReturn(1);

        service.bookAppointment(2, 2, 1, AppointmentType_y.URGENT);
    }
    // ===================== نجاح الحجز =====================
    @Test
    public void testBookAppointment_success() throws Exception {
        // إعداد SELECT
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

        // إعداد INSERT + UPDATE
        when(conn.prepareStatement(contains("INSERT INTO appointments"))).thenReturn(psInsert);
        when(psInsert.executeUpdate()).thenReturn(1);

        when(conn.prepareStatement(contains("UPDATE appointment_slot"))).thenReturn(psUpdate);
        when(psUpdate.executeUpdate()).thenReturn(1);

        // استدعاء الحجز

        // تحقق من استدعاء executeUpdate
    
    }

    // ===================== slot غير موجود =====================
    @Test
    public void testBookAppointment_slotNotFound() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        SQLException ex = assertThrows(SQLException.class, () ->
            service.bookAppointment(10, 999, 2, AppointmentType_y.ONLINE)
        );
        assertTrue(ex.getMessage().contains("Slot not found"));
    }

    // ===================== slot ممتلئ =====================
    @Test
    public void testBookAppointment_slotOverCapacity() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(4); // مكتمل تقريباً

    }

    // ===================== slot في الماضي =====================
    @Test
    public void testBookAppointment_slotInPast() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2000-01-01")); // قديم
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("11:00:00"));
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

        SQLException ex = assertThrows(SQLException.class, () ->
            service.bookAppointment(10, 1, 1, AppointmentType_y.ONLINE)
        );
        assertTrue(ex.getMessage().contains("Cannot book a past slot"));
    }

    // ===================== مدة قصيرة جدا =====================
    @Test
    public void testBookAppointment_durationTooShort() throws Exception {
        when(conn.prepareStatement(contains("SELECT slot_date"))).thenReturn(psSelect);
        when(psSelect.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(rs.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(rs.getTime("end_time")).thenReturn(Time.valueOf("10:10:00")); // 10 دقائق فقط
        when(rs.getInt("max_capacity")).thenReturn(5);
        when(rs.getInt("booked_count")).thenReturn(0);

    }
}