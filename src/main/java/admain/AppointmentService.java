package admain;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private SlotService slotService = new SlotService();
    private final int MIN_DURATION = 30;
    private final int MAX_DURATION = 120;

    public boolean bookAppointment(int slotId, LocalDateTime start, LocalDateTime end, int participants) {
        long duration = Duration.between(start, end).toMinutes();
        if (duration < MIN_DURATION || duration > MAX_DURATION)
            throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");

        try {
            slotService.bookSlot(slotId, start, end, participants);
            System.out.println("Appointment booked successfully!");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Appointment> getAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT slot_id, start_time, end_time, participants, status FROM appointments ORDER BY start_time";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                LocalDateTime start = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime end = rs.getTimestamp("end_time").toLocalDateTime();
                int participants = rs.getInt("participants");
                String status = rs.getString("status");

                Appointment appt = new Appointment(
                        new TimeSlot(slotId, start, end),
                        participants,
                        "General"
                );
                appt.setStatus(status);
                list.add(appt);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}