package admain;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SlotService {

    public List<AppointmentSlot> getAvailableSlots() {

        List<AppointmentSlot> slots = new ArrayList<>();

        String sql = "SELECT * FROM appointment_slot WHERE booked_count < max_capacity";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int id = rs.getInt("slot_id");
                LocalDate date = rs.getDate("slot_date").toLocalDate();
                LocalTime time = rs.getTime("slot_time").toLocalTime();
                int capacity = rs.getInt("max_capacity");
                int booked = rs.getInt("booked_count");

                AppointmentSlot slot = new AppointmentSlot(id, date, time, capacity, booked);
                slots.add(slot);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return slots;
    }
}