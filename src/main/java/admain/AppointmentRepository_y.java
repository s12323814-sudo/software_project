package admain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository_y {
	  private static final ZoneId ZONE = ZoneId.of("Asia/Hebron");
    public List<Appointment> getUpcomingAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();

        String sql = "SELECT a.appointment_id, a.slot_id, a.user_id, a.participants, a.status, " +
                     "a.start_time, a.end_time, s.max_capacity, s.booked_count " +
                     "FROM appointments a " +
                     "JOIN appointment_slot s ON a.slot_id = s.slot_id " +
                     "WHERE a.end_time >= CURRENT_TIMESTAMP";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                int participants = rs.getInt("participants");
                String status = rs.getString("status");
                ZonedDateTime start = rs.getTimestamp("start_time").toInstant().atZone(ZONE);
                ZonedDateTime end = rs.getTimestamp("end_time").toInstant().atZone(ZONE);
String type = "General";


                TimeSlot timeSlot = new TimeSlot(slotId, start, end);

   
                Appointment appt = new Appointment(timeSlot, participants, type);
                appt.setStatus(status);

                appointments.add(appt);
            }
        }

        return appointments;
    }
    public List<Appointment> getUserUpcomingAppointments(int userId) throws SQLException {

        List<Appointment> appointments = new ArrayList<>();

        String sql =
            "SELECT a.appointment_id, a.slot_id, a.start_time, a.end_time, a.participants, a.status " +
            "FROM appointments a " +
            "WHERE a.user_id = ? AND a.end_time >= CURRENT_TIMESTAMP";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int slotId = rs.getInt("slot_id");

                ZonedDateTime start =
                        rs.getTimestamp("start_time").toInstant().atZone(ZONE);

                ZonedDateTime end =
                        rs.getTimestamp("end_time").toInstant().atZone(ZONE);

                TimeSlot slot = new TimeSlot(slotId, start, end);

                Appointment appt = new Appointment(slot,
                        rs.getInt("participants"),
                        "General");

                appt.setStatus(rs.getString("status"));

                appointments.add(appt);
            }
        }

        return appointments;
    }
}
