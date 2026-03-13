package admain;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class scheduleRepository {

    private SlotService_y slotService = new SlotService_y();

    public boolean isSlotAvailable(Connection conn, int slotId, int participants) throws SQLException {
        String sql = "SELECT max_capacity, booked_count FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");
                    return (capacity - booked) >= participants;
                }
            }
        }
        return false;
    }

    public void addAppointment(Connection conn, Appointment appointment) throws SQLException {
        int slotId = appointment.getTimeSlot().getId();
        int participants = appointment.getParticipants();
        ZonedDateTime startZ = appointment.getTimeSlot().getStart();
        ZonedDateTime endZ = appointment.getTimeSlot().getEnd();

        if (!isSlotAvailable(conn, slotId, participants))
            throw new SQLException("Not enough capacity for this slot.");

        long duration = Duration.between(
                appointment.getTimeSlot().getStart(),
                appointment.getTimeSlot().getEnd()
        ).toMinutes();
        if (duration < 30 || duration > 120)
            throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");

        // إدخال Appointment
        String insertSql = "INSERT INTO appointments (slot_id, start_time, end_time, duration, participants, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, slotId);
            ps.setTimestamp(2, Timestamp.valueOf(startZ.toLocalDateTime()));
            ps.setTimestamp(3, Timestamp.valueOf(endZ.toLocalDateTime()));
            ps.setInt(4, (int) duration);
            ps.setInt(5, participants);
            ps.setString(6, "CONFIRMED");
            ps.executeUpdate();
        }

        // تحديث booked_count
        String updateSql = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, participants);
            ps.setInt(2, slotId);
            ps.executeUpdate();
        }
    }// =========================
 // تعديل Appointment
 // =========================
 public void modifyAppointment(int appointmentId, int newSlotId, int newParticipants) throws SQLException {
     try (Connection conn = database_connection.getConnection()) {
         conn.setAutoCommit(false);

         // جلب البيانات القديمة للAppointment
         String oldSql = "SELECT slot_id, participants FROM appointments WHERE appointment_id = ? FOR UPDATE";
         int oldSlotId = 0;
         int oldParticipants = 0;

         try (PreparedStatement ps = conn.prepareStatement(oldSql)) {
             ps.setInt(1, appointmentId);
             ResultSet rs = ps.executeQuery();
             if (!rs.next()) {
                 throw new SQLException("Appointment not found.");
             }
             oldSlotId = rs.getInt("slot_id");
             oldParticipants = rs.getInt("participants");
         }

         // تحقق من السعة الجديدة
         if (!isSlotAvailable(conn, newSlotId, newParticipants)) {
             throw new SQLException("Not enough capacity for the new slot.");
         }

         // تحديث Appointment
         String updateSql = "UPDATE appointments SET slot_id=?, participants=? WHERE appointment_id=?";
         try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
             ps.setInt(1, newSlotId);
             ps.setInt(2, newParticipants);
             ps.setInt(3, appointmentId);
             ps.executeUpdate();
         }

         // تحديث booked_count للSlot القديم والنيو
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
     }
 }
  
 
    public List<Appointment> getAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT slot_id, start_time, end_time, participants, status FROM appointments ORDER BY start_time";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                ZonedDateTime start = rs.getTimestamp("start_time").toInstant().atZone(ZoneId.of("Asia/Hebron"));
                ZonedDateTime end = rs.getTimestamp("end_time").toInstant().atZone(ZoneId.of("Asia/Hebron"));
                int participants = rs.getInt("participants");
                String status = rs.getString("status");

                AppointmentSlot_y slot = slotService.getSlotById(slotId);
                if (slot == null) continue;

                Appointment appt = new Appointment(new TimeSlot(slotId, start, end), participants, "General");
                appt.setStatus(status);
                appointments.add(appt);
            }
        }

        return appointments;
    }
}