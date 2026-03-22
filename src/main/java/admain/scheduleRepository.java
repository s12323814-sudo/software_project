package admain;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class scheduleRepository {

    private static final ZoneId ZONE = ZoneId.of("Asia/Hebron");

    public boolean isSlotAvailable(Connection conn, int slotId, int participants) throws SQLException {
        String sql = "SELECT max_capacity, booked_count FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int capacity = rs.getInt("max_capacity");
                int booked = rs.getInt("booked_count");
                return (capacity - booked) >= participants;
            }
        }
        return false;
    }

    public static AppointmentStatus_y determineStatus(ZonedDateTime start, ZonedDateTime end, int participants, AppointmentSlot_y slot) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Hebron"));

        if (now.isAfter(end)) {
            return AppointmentStatus_y.COMPLETED;  // لو انتهى
        } else if (!now.isBefore(start) && now.isBefore(end)) {
            return AppointmentStatus_y.ONGOING;    // لو جاري
        } else {
            return (slot.getMaxCapacity() - slot.getBookedCount() >= participants)
                    ? AppointmentStatus_y.CONFIRMED
                    : AppointmentStatus_y.WAITLIST; // لو ما بدها حجز
        }
    }
  
    public void addAppointment(Appointment appointment) throws SQLException {
        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int slotId = appointment.getTimeSlot().getId();
                int participants = appointment.getParticipants();
                int userId = appointment.getUserId();
                AppointmentType_y type = appointment.getType(); // النوع يحدده اليوزر

                ZonedDateTime startZ = appointment.getTimeSlot().getStart();
                ZonedDateTime endZ = appointment.getTimeSlot().getEnd();

                long duration = Duration.between(startZ, endZ).toMinutes();
                if (duration < 30 || duration > 120)
                    throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");

               
                AppointmentStatus_y status = determineStatus(conn, slotId, participants, startZ, endZ);

                String insertSql =
                        "INSERT INTO appointments (account_id, slot_id, start_time, end_time, duration, participants, status, type) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, slotId);
                    ps.setTimestamp(3, Timestamp.valueOf(startZ.toLocalDateTime()));
                    ps.setTimestamp(4, Timestamp.valueOf(endZ.toLocalDateTime()));
                    ps.setLong(5, duration);
                    ps.setInt(6, participants);
                    ps.setString(7, status.name()); // الحالة يحسبها النظام
                    ps.setString(8, type.name());   // النوع يحدده اليوزر
                    ps.executeUpdate();
                }

              
                if (status == AppointmentStatus_y.CONFIRMED) {
                    String updateSql = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setInt(1, participants);
                        ps.setInt(2, slotId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                System.out.println("Appointment booked successfully! Status: " + status + ", Type: " + type);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

   
    public void modifyAppointment(int appointmentId, int newSlotId, int newParticipants) throws SQLException {
        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int oldSlotId, oldParticipants;

                String oldSql = "SELECT slot_id, participants FROM appointments WHERE appointment_id = ? FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(oldSql)) {
                    ps.setInt(1, appointmentId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) throw new SQLException("Appointment not found.");
                    oldSlotId = rs.getInt("slot_id");
                    oldParticipants = rs.getInt("participants");
                }

                if (!isSlotAvailable(conn, newSlotId, newParticipants))
                    throw new SQLException("Not enough capacity for the new slot.");

                String updateSql = "UPDATE appointments SET slot_id=?, participants=? WHERE appointment_id=?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, newSlotId);
                    ps.setInt(2, newParticipants);
                    ps.setInt(3, appointmentId);
                    ps.executeUpdate();
                }

            
                String decOldSlot = "UPDATE appointment_slot SET booked_count = booked_count - ? WHERE slot_id=?";
                try (PreparedStatement ps = conn.prepareStatement(decOldSlot)) {
                    ps.setInt(1, oldParticipants);
                    ps.setInt(2, oldSlotId);
                    ps.executeUpdate();
                }

                String incNewSlot = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id=?";
                try (PreparedStatement ps = conn.prepareStatement(incNewSlot)) {
                    ps.setInt(1, newParticipants);
                    ps.setInt(2, newSlotId);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("Appointment modified successfully!");

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    
    public List<Appointment> getAppointments(int userId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT slot_id, start_time, end_time, participants, type FROM appointments WHERE account_id = ? ORDER BY start_time";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                int appointmentId = rs.getInt("appointment_id");
                
                ZonedDateTime start = rs.getTimestamp("start_time").toInstant().atZone(ZONE);
                ZonedDateTime end = rs.getTimestamp("end_time").toInstant().atZone(ZONE);
                int participants = rs.getInt("participants");

                AppointmentType_y type = AppointmentType_y.valueOf(rs.getString("type"));
                AppointmentStatus_y status = determineStatus(conn, slotId, participants, start, end);

                TimeSlot slot = new TimeSlot(slotId, start, end);
                Appointment appt = new Appointment(appointmentId,userId,slotId, slot, participants, status, type); // الكونستركتر كامل
                appointments.add(appt);
            }
        }
        return appointments;
    }

	public AppointmentStatus_y determineStatus(Connection conn, int slotId, int participants, ZonedDateTime startZ,
			ZonedDateTime endZ) {
		// TODO Auto-generated method stub
		return null;
	}
}