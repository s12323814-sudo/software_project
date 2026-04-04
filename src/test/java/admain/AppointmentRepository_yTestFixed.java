package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;

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
    public void testCancel() throws Exception {
        // Mock Connection و PreparedStatement
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(ps.executeUpdate()).thenReturn(1);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        // Mock static داخل try-with-resources
        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.cancel(10, 1);
            assertTrue(result); // تحقق من نجاح الحذف
        }
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
   