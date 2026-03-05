package admain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

   
    public boolean isSlotAvailable(Connection conn, TimeSlot slot) throws SQLException {

        String sql = "SELECT COUNT(*) FROM appointments " +
                     "WHERE slot_id = ? AND status = 'CONFIRMED' FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // 0 → متاح
                }
            }
        }

        return false;
    }

    // =========================
    // إضافة Appointment في DB
    // =========================
    public void addAppointment(Connection conn, Appointment appointment) throws SQLException {

        String sql = "INSERT INTO appointments (slot_id, duration, participants, status) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointment.getTimeSlot().getId());
            ps.setInt(2, appointment.getDuration());
            ps.setInt(3, appointment.getParticipants());
            ps.setString(4, "CONFIRMED");
            ps.executeUpdate();
        }
    }

    public List<Appointment> getAppointments() throws SQLException {

        List<Appointment> appointments = new ArrayList<>();

        String sql = "SELECT slot_id, duration, participants, status FROM appointments";

        try (Connection conn =database_connection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                int duration = rs.getInt("duration");
                int participants = rs.getInt("participants");
                String statusStr = rs.getString("status");

                // جلب الـ TimeSlot من slot_id
                TimeSlot slot = DatabaseUtils.getTimeSlotById(slotId);

                Appointment appointment = new Appointment(slot, participants, "DefaultType");
                appointment.setStatus(AppointmentStatus.valueOf(statusStr));
                appointments.add(appointment);
            }
        }

        return appointments;
    }
}