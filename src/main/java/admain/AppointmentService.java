package admain;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
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

            AppointmentSlot_y slot = slotService.getSlotById(slotId);
            if (slot == null) throw new SQLException("Slot not found.");

            int remaining = slot.getMaxCapacity() - slot.getBookedCount();
            if (participants > remaining)
                throw new SQLException("Not enough capacity. Remaining: " + remaining);

            LocalDateTime start = slot.getStartDateTime();
            LocalDateTime end = slot.getEndDateTime();
            long duration = Duration.between(start, end).toMinutes();

            if (duration < MIN_DURATION || duration > MAX_DURATION)
                throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");

            // إنشاء Appointment
            Appointment appt = new Appointment(new TimeSlot(slotId, start, end), participants, "General");

            // إضافة الحجز في DB
            repo.addAppointment(conn, appt);

            conn.commit();

            System.out.println("Appointment booked successfully!");
            System.out.println("Remaining capacity: " + (remaining - participants));
        } catch (Exception e) {
            throw new SQLException("Booking failed: " + e.getMessage(), e);
        }
    }

    // =========================
    // جلب كل المواعيد
    // =========================
    public List<Appointment> getAppointments() throws SQLException {
        return repo.getAppointments();
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