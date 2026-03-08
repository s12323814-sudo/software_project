package admain;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SlotService {

   
    public List<AppointmentSlot> getAvailableSlots() {
        List<AppointmentSlot> slots = new ArrayList<>();

        String sql = "SELECT * FROM appointment_slots WHERE booked_count < max_capacity";

        try (Connection conn = database_connection.getConnection();
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

 
    public AppointmentSlot getSlotById(int slotId) {
        String sql = "SELECT * FROM appointment_slots WHERE slot_id = ?";
        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("slot_id");
                    LocalDate date = rs.getDate("slot_date").toLocalDate();
                    LocalTime time = rs.getTime("slot_time").toLocalTime();
                    int maxCap = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");
                    return new AppointmentSlot(id, date, time, maxCap, booked);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

   
    public void bookSlotForUser(int userId, int slotId, int participants) throws SQLException {
        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);

            
            String checkSql = "SELECT max_capacity, booked_count FROM appointment_slots WHERE slot_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, slotId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Slot not found.");
                    }

                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");
                    int remaining = capacity - booked;

                    if (participants > remaining) {
                        throw new SQLException("Not enough capacity for this slot. Remaining: " + remaining);
                    }

                    String insertSql = "INSERT INTO booking(user_id, slot_id, participants) VALUES (?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, userId);
                        insertPs.setInt(2, slotId);
                        insertPs.setInt(3, participants);
                        insertPs.executeUpdate();
                    }
                    String updateSql = "UPDATE appointment_slots SET booked_count = booked_count + ? WHERE slot_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, participants);
                        updatePs.setInt(2, slotId);
                        updatePs.executeUpdate();
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            throw e;
        }
    }

    public void bookSlot(int slotId, int participants) throws SQLException {

        try (Connection conn = database_connection.getConnection()) {

            conn.setAutoCommit(false);

            String checkSql = "SELECT max_capacity, booked_count FROM appointment_slots WHERE slot_id = ? FOR UPDATE";

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {

                ps.setInt(1, slotId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new IllegalArgumentException("Slot not found.");
                    }

                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");

                    if (booked + participants > capacity) {
                        throw new IllegalArgumentException("Participant limit exceeded.");
                    }

                    String insertSql = "INSERT INTO booking(slot_id, participants, status) VALUES (?, ?, ?)";

                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {

                        insertPs.setInt(1, slotId);
                        insertPs.setInt(2, participants);
                        insertPs.setString(3, "CONFIRMED");

                        insertPs.executeUpdate();
                    }

                    String updateSql = "UPDATE appointment_slots SET booked_count = booked_count + ? WHERE slot_id = ?";

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