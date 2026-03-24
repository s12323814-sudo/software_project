package admain;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SlotRepository_y {

    /////////////////////////////
    public List<AppointmentSlot_y> findAvailableSlots() {

        List<AppointmentSlot_y> list = new ArrayList<>();

        String sql = "SELECT * FROM appointment_slot";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                AppointmentSlot_y slot = map(rs);

                if (slot.getBookedCount() < slot.getMaxCapacity()) {
                    list.add(slot);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /////////////////////////////
    public AppointmentSlot_y findById(int id) {

        String sql = "SELECT * FROM appointment_slot WHERE slot_id = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
    public List<AppointmentSlot_y> findAvailableSlotsByDate(LocalDate date) {

        List<AppointmentSlot_y> list = new ArrayList<>();

        String sql = "SELECT * FROM appointment_slot WHERE slot_date = ? AND booked_count < max_capacity";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    /////////////////////////////
    public boolean addSlot(LocalDate date, LocalTime start, LocalTime end,
            int capacity, int accountId) {

String sql = "INSERT INTO appointment_slot " +
      "(slot_date, slot_start_time, slot_end_time, max_capacity, booked_count, account_id) " +
      "VALUES (?, ?, ?, ?, 0, ?)";

try (Connection conn = database_connection.getConnection();
PreparedStatement ps = conn.prepareStatement(sql)) {

ps.setDate(1, Date.valueOf(date));
ps.setTime(2, Time.valueOf(start));
ps.setTime(3, Time.valueOf(end));
ps.setInt(4, capacity);
ps.setInt(5, accountId);

return ps.executeUpdate() > 0;

} catch (SQLException e) {
throw new RuntimeException(e);
}
}
    /////////////////////////////
    public void decreaseBookedCount(int slotId, int participants, Connection conn) throws SQLException {

        String sql = "UPDATE appointment_slot SET booked_count = booked_count - ? WHERE slot_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participants);
            ps.setInt(2, slotId);
            ps.executeUpdate();
        }
    }

    /////////////////////////////
    private AppointmentSlot_y map(ResultSet rs) throws SQLException {
        return new AppointmentSlot_y(
                rs.getInt("slot_id"),
                rs.getDate("slot_date").toLocalDate(),
                rs.getTime("slot_start_time").toLocalTime(),
                rs.getTime("slot_end_time").toLocalTime(),
                rs.getInt("max_capacity"),
                rs.getInt("booked_count")

        );
    }
}