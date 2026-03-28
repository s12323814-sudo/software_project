package admain;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository_y {
	Connection conn = database_connection.getConnection();
    private static final ZoneId ZONE = ZoneId.of("Asia/Hebron");
    private SlotRepository_y slotRepo = new SlotRepository_y();
  
    public List<Appointment> getUpcomingAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();

        String sql =
                "SELECT a.appointment_id, a.slot_id, a.account_id, a.participants, a.status, a.type, " +
                "a.start_time, a.end_time, s.max_capacity, s.booked_count " +
                "FROM appointments a " +
                "JOIN appointment_slot s ON a.slot_id = s.slot_id " +
                "WHERE a.end_time >= CURRENT_TIMESTAMP";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
            	 int appointmentId = rs.getInt("appointment_id");
                int userId = rs.getInt("account_id");
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
            "SELECT a.appointment_id, a.slot_id, a.account_id, a.participants, a.status, a.type, a.start_time, a.end_time " +
            "FROM appointments a " +
            "WHERE a.account_id = ? " +
            "ORDER BY a.start_time ASC"; // ترتيب من الأقدم للأحدث

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                int appointmentId = rs.getInt("appointment_id");
                int participants = rs.getInt("participants");
                AppointmentStatus_y status = AppointmentStatus_y.valueOf(rs.getString("status"));
                AppointmentType_y type = AppointmentType_y.valueOf(rs.getString("type"));
                ZonedDateTime start = rs.getTimestamp("start_time").toInstant()
                                        .atZone(ZoneId.of("Asia/Hebron"));
                ZonedDateTime end = rs.getTimestamp("end_time").toInstant()
                                      .atZone(ZoneId.of("Asia/Hebron"));

                TimeSlot timeSlot = new TimeSlot(slotId, start, end);
                Appointment appt = new Appointment(appointmentId, userId, slotId, timeSlot, participants, status, type);

                // فلترة المواعيد المنتهية تلقائياً
                if (end.isAfter(ZonedDateTime.now(ZoneId.of("Asia/Hebron")))) {
                    appointments.add(appt);
                }
            }
        }

        return appointments;
    }

    // دالة مساعدة للعرض
    public static void displayAppointments(List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            System.out.println("No upcoming appointments.");
            return;
        }

        for (Appointment appt : appointments) {
            System.out.printf(
                "ID: %d | Slot: %d | Participants: %d | Type: %s | Status: %s | Start: %s | End: %s | Duration: %d mins%n",
                appt.getUserId(),
                appt.getSlotId(),
                appt.getParticipants(),
                appt.getType(),
                appt.getStatus(),
                appt.getTimeSlot().getStart().toLocalDateTime(),
                appt.getTimeSlot().getEnd().toLocalDateTime(),
                appt.getTimeSlot().getDurationMinutes()
            );
        }
    }

    public Appointment findById(int id, Connection conn) throws SQLException {

        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
           
            if (rs.next()) {

                TimeSlot slot = new TimeSlot(
                        rs.getInt("slot_id"),
                        rs.getTimestamp("start_time")
                          .toInstant().atZone(java.time.ZoneId.systemDefault()),
                        rs.getTimestamp("end_time")
                          .toInstant().atZone(java.time.ZoneId.systemDefault())
                );

                return new Appointment(
                		rs.getInt("appointment_id"),
                        rs.getInt("account_id"),
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
    protected Connection getConnection() throws SQLException {
        return database_connection.getConnection();
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
    public boolean book(int userId, int slotId, int participants, AppointmentType_y type) throws SQLException {
        AppointmentSlot_y slot = slotRepo.findById(slotId);
        if (slot == null) return false;

        // تحويل الوقت إلى ZonedDateTime
        ZonedDateTime startZ = ZonedDateTime.of(slot.getDate(), slot.getStartTime(), ZoneId.of("Asia/Hebron"));
        ZonedDateTime endZ = ZonedDateTime.of(slot.getDate(), slot.getEndTime(), ZoneId.of("Asia/Hebron"));

        // منع حجز المواعيد المنتهية
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Hebron"));
        if (endZ.isBefore(now)) {
            System.out.println("Cannot book this slot: it has already ended.");
            return false;
        }

        // حساب المدة بالدقائق
        long duration = Duration.between(startZ, endZ).toMinutes();

        // تحويل ZonedDateTime إلى Timestamp للـ PreparedStatement
        Timestamp startTimestamp = Timestamp.from(startZ.toInstant());
        Timestamp endTimestamp = Timestamp.from(endZ.toInstant());

        try (Connection conn = database_connection.getConnection()) {
            // تحديد حالة الحجز يدوياً بدل استدعاء determineStatus غير المرئي
            AppointmentStatus_y status = (slot.getBookedCount() + participants <= slot.getMaxCapacity()) ?
                                         AppointmentStatus_y.CONFIRMED :
                                         AppointmentStatus_y.WAITLIST;

            String sql = "INSERT INTO appointments " +
                         "(account_id, slot_id, start_time, end_time, duration, participants, status, type) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, slotId);
                ps.setTimestamp(3, startTimestamp);
                ps.setTimestamp(4, endTimestamp);
                ps.setLong(5, duration);
                ps.setInt(6, participants);
                ps.setString(7, status.name());
                ps.setString(8, type.name());
                int rows = ps.executeUpdate();

                // تحديث booked_count إذا تم التأكيد
                if (status == AppointmentStatus_y.CONFIRMED) {
                    String updateSlot = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                    try (PreparedStatement ps2 = conn.prepareStatement(updateSlot)) {
                        ps2.setInt(1, participants);
                        ps2.setInt(2, slotId);
                        ps2.executeUpdate();
                    }
                }

                System.out.println("Appointment booked successfully! Status: " + status + ", Type: " + type);
                return rows > 0;
            }
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