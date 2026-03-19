package admain;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

import java.util.List;

public class AppointmentService {
	 private AppointmentRepository_y appointmentRepo;
	    private SlotRepository_y slotRepo;
    private final SlotService_y slotService = new SlotService_y(appointmentRepo,slotRepo);
    private final scheduleRepository repo = new scheduleRepository();
    private final int MIN_DURATION = 30;
    private final int MAX_DURATION = 120;

  
    public void bookAppointment(int userId, int slotId, int participants, AppointmentType_y type) throws SQLException {
        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);

      
            String sql = "SELECT start_time, end_time, max_capacity, booked_count " +
                         "FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, slotId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Slot not found");

                    LocalDateTime start = rs.getTimestamp("start_time").toLocalDateTime();
                    LocalDateTime end = rs.getTimestamp("end_time").toLocalDateTime();
                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");

                    if (start.isBefore(LocalDateTime.now()))
                        throw new SQLException("Cannot book a past slot");

                    int remaining = capacity - booked;
                    if (participants > remaining)
                        throw new SQLException("Not enough capacity for this slot");

                    long duration = Duration.between(start, end).toMinutes();
                    if (duration < MIN_DURATION || duration > MAX_DURATION)
                        throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");

                    String insert = "INSERT INTO appointments " +
                                    "(account_id, slot_id, start_time, end_time, duration, participants, status, type) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement psInsert = conn.prepareStatement(insert)) {
                        psInsert.setInt(1, userId);
                        psInsert.setInt(2, slotId);
                        psInsert.setTimestamp(3, Timestamp.valueOf(start));
                        psInsert.setTimestamp(4, Timestamp.valueOf(end));
                        psInsert.setInt(5, (int) duration);
                        psInsert.setInt(6, participants);
                        psInsert.setString(7, AppointmentStatus_y.CONFIRMED.name()); // حالة افتراضية
                        psInsert.setString(8, type.name()); // النوع المرسل كـ parameter
                        psInsert.executeUpdate();
                    }

                    // تحديث booked_count
                    String update = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(update)) {
                        psUpdate.setInt(1, participants);
                        psUpdate.setInt(2, slotId);
                        psUpdate.executeUpdate();
                    }

                    conn.commit();
                    System.out.println("Appointment booked successfully! Remaining capacity: " + (remaining - participants));
                }
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                throw e;
            }
        }
    }


    public List<Appointment> getUserAppointments(int userId) throws SQLException {
        return repo.getAppointments(userId);
    }

 
    public List<AppointmentSlot_y> getAvailableSlots() {
        return slotService.getAvailableSlots();
    }

  
    public void addSlot(LocalDateTime start, LocalDateTime end, int capacity, int adminId) {
        slotService.addSlot(start.toLocalDate(), start.toLocalTime(), end.toLocalTime(), capacity, adminId);
    }
}
