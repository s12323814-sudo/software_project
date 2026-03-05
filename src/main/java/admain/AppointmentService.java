package admain;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private ScheduleRepository repository = new ScheduleRepository();

    private final int MIN_DURATION = 30;  // دقيقة
    private final int MAX_DURATION = 120; // دقيقة

    public boolean bookAppointment(int slotId, LocalDateTime start, LocalDateTime end, int participants) {

        long durationMinutes = Duration.between(start, end).toMinutes();

        if (durationMinutes < MIN_DURATION || durationMinutes > MAX_DURATION) {
            throw new IllegalArgumentException("Duration must be between " + MIN_DURATION + " and " + MAX_DURATION + " minutes.");
        }

        try (Connection conn = database_connection.getConnection()) {

            conn.setAutoCommit(false);

            // 🔹 Lock row للتحقق من availability
            String checkSql = "SELECT max_capacity, booked_count FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, slotId);

                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Slot ID not found.");
                    }

                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");

                    if (booked + participants > capacity) {
                        throw new IllegalArgumentException("Participant limit exceeded.");
                    }

                    // 🔹 Insert appointment
                    String insertSql = "INSERT INTO appointments (slot_id, start_time, end_time, duration, participants, status) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        ps.setInt(1, slotId);
                        ps.setTimestamp(2, Timestamp.valueOf(start));
                        ps.setTimestamp(3, Timestamp.valueOf(end));
                        ps.setInt(4, (int) durationMinutes);
                        ps.setInt(5, participants);
                        ps.setString(6, "CONFIRMED");
                        ps.executeUpdate();
                    }

                    // 🔹 تحديث booked_count في slot
                    String updateSql = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, participants);
                        updatePs.setInt(2, slotId);
                        updatePs.executeUpdate();
                    }
                }
            }

            conn.commit();
            System.out.println("Appointment booked successfully!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===============================
    // جلب كل المواعيد من DB
    // ===============================
    public List<Appointment> getAppointments() {

        List<Appointment> appointments = new ArrayList<>();

        String sql = "SELECT a.slot_id, a.start_time, a.end_time, a.duration, a.participants, a.status " +
                     "FROM appointments a ORDER BY a.start_time";

        try (Connection conn =database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                LocalDateTime start = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime end = rs.getTimestamp("end_time").toLocalDateTime();
                int duration = rs.getInt("duration");
                int participants = rs.getInt("participants");
                String status = rs.getString("status");

                TimeSlot slot = new TimeSlot(slotId, start, end);
                Appointment appointment = new Appointment(slot, participants, "General");
                appointment.setStatus(AppointmentStatus.valueOf(status));

                appointments.add(appointment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }
}