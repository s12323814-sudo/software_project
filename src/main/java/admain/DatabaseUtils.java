package admain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DatabaseUtils {

    public static TimeSlot getTimeSlotById(int slotId) throws SQLException {

        String sql = "SELECT slot_id, slot_time FROM appointment_slot WHERE slot_id = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("slot_id");
                    LocalDateTime start = rs.getTimestamp("slot_time").toLocalDateTime();
                    LocalDateTime end = start.plusMinutes(60); // افتراضياً مدة ساعة، يمكن تعديلها حسب الحاجة

                    return new TimeSlot(id, start, end);
                } else {
                    throw new IllegalArgumentException("Slot ID not found in DB.");
                }
            }
        }
    }
}