package admain;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SlotService {

    // ===============================
    // 1️⃣ View Available Slots
    // ===============================
    public List<AppointmentSlot> getAvailableSlots() {

        List<AppointmentSlot> slots = new ArrayList<>();

        String sql = "SELECT * FROM appointment_slot WHERE booked_count < max_capacity";

        try (Connection conn =  database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int id = rs.getInt("slot_id");
                LocalDate date = rs.getDate("slot_date").toLocalDate();
                LocalTime time = rs.getTime("slot_time").toLocalTime();
                int maxCap = rs.getInt("max_capacity");
                int booked = rs.getInt("booked_count");

                AppointmentSlot slot = new AppointmentSlot(id, date, time, maxCap, booked);
                slots.add(slot);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return slots;
    }

    // ===============================
    // 2️⃣ Book Appointment (Start & End Time)
    // ===============================
    public void bookSlot(int slotId, LocalDateTime start, LocalDateTime end, int participants) throws SQLException {

        long duration = java.time.Duration.between(start, end).toMinutes();

        if (duration < 30 || duration > 120) {
            throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");
        }

        try (Connection conn =  database_connection.getConnection()) {
            conn.setAutoCommit(false);

            // Lock row
            String checkSql = "SELECT max_capacity, booked_count FROM appointment_slot WHERE slot_id = ? FOR UPDATE";

            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, slotId);

                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Slot not found.");
                    }

                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");

                    if (booked + participants > capacity) {
                        throw new IllegalArgumentException("Participant limit exceeded.");
                    }

                    // Insert Appointment
                    String insertSql = "INSERT INTO appointments (slot_id, start_time, end_time, duration, participants, status) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        ps.setInt(1, slotId);
                        ps.setTimestamp(2, Timestamp.valueOf(start));
                        ps.setTimestamp(3, Timestamp.valueOf(end));
                        ps.setInt(4, (int) duration);
                        ps.setInt(5, participants);
                        ps.setString(6, "CONFIRMED");

                        ps.executeUpdate();
                    }

                    // Update booked_count
                    String updateSql = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, participants);
                        updatePs.setInt(2, slotId);
                        updatePs.executeUpdate();
                    }
                }
            }

            conn.commit();

        } catch (Exception e) {
            throw e;
        }
    }
}