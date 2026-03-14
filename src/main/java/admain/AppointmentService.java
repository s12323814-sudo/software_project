package admain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class AppointmentService {

    private final SlotService_y slotService = new SlotService_y();
    private final scheduleRepository repo = new scheduleRepository();
    private final int MIN_DURATION = 30;
    private final int MAX_DURATION = 120;

    // =========================
    // حجز موعد للمستخدم
    // =========================
    public void bookAppointment(int userId, int slotId, int participants) throws SQLException {
        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);

            // جلب الـ slot مع lock
            String sql = "SELECT start_time, end_time, max_capacity, booked_count " +
                         "FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                throw new SQLException("Slot not found");

            LocalDateTime start = rs.getTimestamp("start_time").toLocalDateTime();
            LocalDateTime end = rs.getTimestamp("end_time").toLocalDateTime();
            int capacity = rs.getInt("max_capacity");
            int booked = rs.getInt("booked_count");

            // منع الحجز بالماضي
            if (start.isBefore(LocalDateTime.now()))
                throw new SQLException("Cannot book a past slot");

            int remaining = capacity - booked;
            if (participants > remaining)
                throw new SQLException("Not enough capacity");

            long duration = Duration.between(start, end).toMinutes();
            if (duration < MIN_DURATION || duration > MAX_DURATION)
                throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");

            // إدخال Appointment مع user_id
            String insert = "INSERT INTO appointments " +
                            "(user_id, slot_id, start_time, end_time, duration, participants, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psInsert = conn.prepareStatement(insert);
            psInsert.setInt(1, userId);
            psInsert.setInt(2, slotId);
            psInsert.setTimestamp(3, Timestamp.valueOf(start));
            psInsert.setTimestamp(4, Timestamp.valueOf(end));
            psInsert.setInt(5, (int) duration);
            psInsert.setInt(6, participants);
            psInsert.setString(7, "CONFIRMED");
            psInsert.executeUpdate();

            // تحديث booked_count
            String update = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
            PreparedStatement psUpdate = conn.prepareStatement(update);
            psUpdate.setInt(1, participants);
            psUpdate.setInt(2, slotId);
            psUpdate.executeUpdate();

            conn.commit();

            System.out.println("Appointment booked successfully!");
            System.out.println("Remaining capacity: " + (remaining - participants));
        }
    }

    // =========================
    // جلب مواعيد المستخدم فقط
    // =========================
    public List<Appointment> getUserAppointments(int userId) throws SQLException {
        return repo.getAppointments(userId);
    }

    // =========================
    // جلب كل الـ Slots المتاحة
    // =========================
    public List<AppointmentSlot_y> getAvailableSlots() {
        return slotService.getAvailableSlots();
    }

    // =========================
    // إضافة Slot جديد (Admin)
    // =========================
    public void addSlot(LocalDateTime start, LocalDateTime end, int capacity, int adminId) {
        slotService.addSlot(start.toLocalDate(), start.toLocalTime(), end.toLocalTime(), capacity, adminId);
    }
}
