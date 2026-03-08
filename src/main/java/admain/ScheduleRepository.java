package admain;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

    private SlotService slotService = new SlotService();

    // =========================
    // التحقق إذا Slot متاح
    // =========================
    public boolean isSlotAvailable(Connection conn, int slotId, int participants) throws SQLException {
        String sql = "SELECT max_capacity, booked_count FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");
                    return (capacity - booked) >= participants;
                }
            }
        }
        return false;
    }

    // =========================
    // إضافة Appointment في DB
    // =========================
    public void addAppointment(Connection conn, Appointment appointment) throws SQLException {
        // التحقق من مدة الحجز
        long duration = Duration.between(
                appointment.getTimeSlot().getStart(),
                appointment.getTimeSlot().getEnd()
        ).toMinutes();
        if (duration < 30 || duration > 120) {
            throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");
        }

        // تحقق من السعة
        if (!isSlotAvailable(conn, appointment.getTimeSlot().getId(), appointment.getParticipants())) {
            throw new SQLException("Not enough capacity for this slot.");
        }

        // إدخال الحجز
        String insertSql = "INSERT INTO appointments (slot_id, start_time, end_time, duration, participants, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, appointment.getTimeSlot().getId());
            ps.setTimestamp(2, Timestamp.valueOf(appointment.getTimeSlot().getStart()));
            ps.setTimestamp(3, Timestamp.valueOf(appointment.getTimeSlot().getEnd()));
            ps.setInt(4, (int) duration);
            ps.setInt(5, appointment.getParticipants());
            ps.setString(6, "CONFIRMED");
            ps.executeUpdate();
        }

        // تحديث booked_count
        String updateSql = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, appointment.getParticipants());
            ps.setInt(2, appointment.getTimeSlot().getId());
            ps.executeUpdate();
        }
    }

    // =========================
    // جلب كل Appointments
    // =========================
    public List<Appointment> getAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT slot_id, start_time, end_time, participants, status FROM appointments ORDER BY start_time";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                LocalDateTime start = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime end = rs.getTimestamp("end_time").toLocalDateTime();
                int participants = rs.getInt("participants");
                String statusStr = rs.getString("status");

                // جلب الـ AppointmentSlot من slot_id
                AppointmentSlot slot = slotService.getSlotById(slotId);
                if (slot == null) continue; // إذا لم يوجد Slot

                Appointment appointment = new Appointment(
                        new TimeSlot(slotId, start, end),
                        participants,
                        "General"
                );
                appointment.setStatus(statusStr);
                appointments.add(appointment);
            }
        }

        return appointments;
    }
}