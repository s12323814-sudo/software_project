package admain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class YourService {

    private SlotRepository_y repo;

    public YourService(SlotRepository_y repo) {
        this.repo = repo;
    }

    public boolean isSlotAvailableForResource(int slotId, int resourceId) {
        String sql = "SELECT COUNT(*) FROM appointment_slot a " +
                     "JOIN appointment b ON a.slot_id = b.slot_id " +
                     "WHERE a.slot_id = ? AND b.resource_id = ?";

        try (Connection conn = repo.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);
            ps.setInt(2, resourceId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}