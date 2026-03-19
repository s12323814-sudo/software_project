package admain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository_y {

    private static final ZoneId ZONE = ZoneId.of("Asia/Hebron");

  
    public List<Appointment> getUpcomingAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();

        String sql =
                "SELECT a.appointment_id, a.slot_id, a.user_id, a.participants, a.status, a.type, " +
                "a.start_time, a.end_time, s.max_capacity, s.booked_count " +
                "FROM appointments a " +
                "JOIN appointment_slot s ON a.slot_id = s.slot_id " +
                "WHERE a.end_time >= CURRENT_TIMESTAMP";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
            	 int appointmentId = rs.getInt("appointment_id");
                int userId = rs.getInt("user_id");
                int slotId = rs.getInt("slot_id");
                int participants = rs.getInt("participants");

          
                AppointmentStatus_y status = AppointmentStatus_y.valueOf(rs.getString("status"));

              
                AppointmentType_y type = AppointmentType_y.valueOf(rs.getString("type"));

                ZonedDateTime start = rs.getTimestamp("start_time").toInstant().atZone(ZONE);
                ZonedDateTime end = rs.getTimestamp("end_time").toInstant().atZone(ZONE);

                TimeSlot timeSlot = new TimeSlot(slotId, start, end);

                Appointment appt = new Appointment(appointmentId,userId,slotId, timeSlot, participants, status, type);
                appointments.add(appt);
            }
        }

        return appointments;
    }

  
    public List<Appointment> getUserUpcomingAppointments(int userId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();

        String sql =
                "SELECT a.appointment_id,a.slot_id, a.user_id, a.participants, a.status, a.type, a.start_time, a.end_time " +
                "FROM appointments a " +
                "WHERE a.user_id = ? AND a.end_time >= CURRENT_TIMESTAMP";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
             
            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                int participants = rs.getInt("participants");
                int AppointmentId =rs.getInt("appointment_id");
                AppointmentStatus_y status = AppointmentStatus_y.valueOf(rs.getString("status"));
                AppointmentType_y type = AppointmentType_y.valueOf(rs.getString("type"));

                ZonedDateTime start = rs.getTimestamp("start_time").toInstant().atZone(ZONE);
                ZonedDateTime end = rs.getTimestamp("end_time").toInstant().atZone(ZONE);

                TimeSlot timeSlot = new TimeSlot(slotId, start, end);
                Appointment appt = new Appointment(AppointmentId , userId,slotId, timeSlot, participants, status, type);

                appointments.add(appt);
            }
        }

        return appointments;
    }


    public Appointment findById(int id, Connection conn) throws SQLException {

        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
           
            if (rs.next()) {

                TimeSlot slot = new TimeSlot(
                        rs.getInt("slot_id"),
                        rs.getTimestamp("slot_start_time")
                          .toInstant().atZone(java.time.ZoneId.systemDefault()),
                        rs.getTimestamp("slot_end_time")
                          .toInstant().atZone(java.time.ZoneId.systemDefault())
                );

                return new Appointment(
                		rs.getInt("appointment_id"),
                        rs.getInt("user_id"),
                        rs.getInt("slot_id"),
                        slot, 
                        rs.getInt("participants"),
                        AppointmentStatus_y.valueOf(rs.getString("status")),
                        AppointmentType_y.valueOf(rs.getString("type"))
                );
            }
        }

        return null;
    }

    /////////////////////////////////

    public void delete(int id, Connection conn) throws SQLException {

        String sql = "DELETE FROM appointments WHERE appointment_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
 // BOOK
    public boolean book(int userId, int slotId, int participants) throws SQLException {
        String sql = "INSERT INTO appointments(account_id, slot_id, participants, status, type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, slotId);
            ps.setInt(3, participants);
            ps.setString(4, AppointmentStatus_y.ONGOING.name()); // default status
            ps.setString(5, AppointmentType_y.GENERAL.name()); // مثال
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    // CANCEL
    public boolean cancel(int userId, int appointmentId) throws SQLException {
        String sql = "DELETE FROM appointments WHERE appointment_id = ? AND account_id = ?";
        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    // UPDATE
    public boolean update(int userId, int appointmentId, int participants) throws SQLException {
        String sql = "UPDATE appointments SET participants = ? WHERE appointment_id = ? AND account_id = ?";
        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, participants);
            ps.setInt(2, appointmentId);
            ps.setInt(3, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }
}