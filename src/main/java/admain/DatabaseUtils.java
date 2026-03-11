package admain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
public class DatabaseUtils {

    public static TimeSlot getTimeSlotById(int slotId) throws SQLException {
        String sql = "SELECT slot_date, slot_time FROM appointment_slot WHERE slot_id = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime start = rs.getDate("slot_date").toLocalDate()
                            .atTime(rs.getTime("slot_time").toLocalTime());
                    LocalDateTime end = start.plusMinutes(60); // مدة افتراضية
                    return new TimeSlot(slotId, start, end);
                } else {
                    throw new IllegalArgumentException("Slot not found");
                }
            }
        }
    }
}