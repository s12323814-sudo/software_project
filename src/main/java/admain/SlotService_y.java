package admain;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SlotService_y {

    
    public List<AppointmentSlot_y> getAvailableSlots() {
        List<AppointmentSlot_y> slots = new ArrayList<>();
        String sql = "SELECT * FROM appointment_slot WHERE booked_count < max_capacity";

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

                AppointmentSlot_y slot = new AppointmentSlot_y(id, date, startTime, endTime, maxCap, booked);
                slots.add(slot);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return slots;
    }

 
    public AppointmentSlot_y getSlotById(int slotId) {
        String sql = "SELECT * FROM appointment_slot WHERE slot_id = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("slot_id");
                    LocalDate date = rs.getDate("slot_date").toLocalDate();
                    LocalTime startTime = rs.getTime("slot_start_time").toLocalTime();
                    LocalTime endTime = rs.getTime("slot_end_time").toLocalTime();
                    int maxCap = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");

                    return new AppointmentSlot_y(id, date, startTime, endTime, maxCap, booked);
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

            String checkSql = "SELECT max_capacity, booked_count, slot_date, slot_start_time, slot_end_time FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, slotId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Slot not found.");
                    }

                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");
                    LocalDate date = rs.getDate("slot_date").toLocalDate();
                    LocalTime startTime = rs.getTime("slot_start_time").toLocalTime();
                    LocalTime endTime = rs.getTime("slot_end_time").toLocalTime();

                    int remaining = capacity - booked;
                    if (participants > remaining) {
                        throw new SQLException("Not enough capacity. Remaining: " + remaining);
                    }

                    LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
                    LocalDateTime endDateTime = LocalDateTime.of(date, endTime);
                    long durationMinutes = java.time.Duration.between(startDateTime, endDateTime).toMinutes();

                    String insertSql = "INSERT INTO appointments(slot_id, user_id, participants, status, start_time, end_time, duration) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, slotId);
                        insertPs.setInt(2, userId);
                        insertPs.setInt(3, participants);
                        insertPs.setString(4, "CONFIRMED");
                        insertPs.setTimestamp(5, Timestamp.valueOf(startDateTime));
                        insertPs.setTimestamp(6, Timestamp.valueOf(endDateTime));
                        insertPs.setLong(7, durationMinutes);
                        insertPs.executeUpdate();
                    }

                    
                    String updateSql = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
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

            String checkSql = "SELECT max_capacity, booked_count, slot_start_time, slot_end_time, slot_date FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, slotId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Slot not found.");
                    }

                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");
                    LocalTime startTime = rs.getTime("slot_start_time").toLocalTime();
                    LocalTime endTime = rs.getTime("slot_end_time").toLocalTime();
                    LocalDate date = rs.getDate("slot_date").toLocalDate();

                    if (booked + participants > capacity) {
                        throw new IllegalArgumentException("Participant limit exceeded.");
                    }

                    LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
                    LocalDateTime endDateTime = LocalDateTime.of(date, endTime);
                    long durationMinutes = java.time.Duration.between(startDateTime, endDateTime).toMinutes();
                    String insertSql = "INSERT INTO appointments(slot_id, participants, status, start_time, end_time,duration) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, slotId);
                        insertPs.setInt(2, participants);
                        insertPs.setString(3, "CONFIRMED");
                        insertPs.setTimestamp(4, Timestamp.valueOf(startDateTime));
                        insertPs.setTimestamp(5, Timestamp.valueOf(endDateTime));
                        insertPs.setLong(6, durationMinutes); 
                        
                        insertPs.executeUpdate();
                    }

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