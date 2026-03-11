package admain;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SlotService {

    // =========================
    // إضافة Slot (Start و End) مع adminId
    // =========================
    public void addSlot(LocalDate date, LocalTime startTime, LocalTime endTime, int capacity, int adminId) {
        String sql = "INSERT INTO appointment_slot (slot_date, slot_start_time, slot_end_time, max_capacity, booked_count, admin_id) " +
                     "VALUES (?, ?, ?, ?, 0, ?)";
        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setTime(2, Time.valueOf(startTime));
            ps.setTime(3, Time.valueOf(endTime));
            ps.setInt(4, capacity);
            ps.setInt(5, adminId);

            ps.executeUpdate();
            System.out.println("Slot added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // جلب كل Slots المتاحة
    // =========================
    public List<AppointmentSlot_y> getAvailableSlots() {
        List<AppointmentSlot_y> slots = new ArrayList<>();
        String sql = "SELECT slot_id, slot_date, slot_start_time, slot_end_time, max_capacity, booked_count FROM appointment_slot ORDER BY slot_date, slot_start_time";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("slot_id");
                LocalDate date = rs.getDate("slot_date").toLocalDate();
                LocalTime startTime = rs.getTime("slot_start_time").toLocalTime();
                LocalTime endTime = rs.getTime("slot_end_time").toLocalTime();
                int maxCap = rs.getInt("max_capacity");
                int booked = rs.getInt("booked_count");

                slots.add(new AppointmentSlot_y(id, date, startTime, endTime, maxCap, booked));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return slots;
    }

    // =========================
    // جلب Slot حسب ID
    // =========================
    public AppointmentSlot_y getSlotById(int slotId) {
        for (AppointmentSlot_y slot : getAvailableSlots()) {
            if (slot.getId() == slotId) return slot;
        }
        return null;
    }

    // =========================
    // حجز Slot لمستخدم
    // =========================
    public void bookSlot(int slotId, LocalDateTime start, LocalDateTime end, int participants) throws SQLException {
        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);

            // تأكيد وجود Slot مع القفل FOR UPDATE
            String checkSql = "SELECT max_capacity, booked_count FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, slotId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Slot not found.");

                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");
                    if (participants > (capacity - booked))
                        throw new SQLException("Not enough capacity.");

                    // إضافة Appointment
                    String insertSql = "INSERT INTO appointments (slot_id, start_time, end_time, duration, participants, status) VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, slotId);
                        insertPs.setTimestamp(2, Timestamp.valueOf(start));
                        insertPs.setTimestamp(3, Timestamp.valueOf(end));
                        insertPs.setInt(4, (int) java.time.Duration.between(start, end).toMinutes());
                        insertPs.setInt(5, participants);
                        insertPs.executeUpdate();
                    }

                    // تحديث booked_count
                    String updateSql = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, participants);
                        updatePs.setInt(2, slotId);
                        updatePs.executeUpdate();
                    }
                }
            }
            conn.commit();
        }
    }
}