package admain;

import java.sql.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AppointmentRepository {

    private static final Logger logger =
            Logger.getLogger(AppointmentRepository.class.getName());

    private static final ZoneId ZONE = ZoneId.of("Asia/Hebron");

    private SlotRepository_y slotRepo;

    public AppointmentRepository() {
        this.slotRepo = new SlotRepository_y();
    }

    public AppointmentRepository(SlotRepository_y slotRepo) {
        this.slotRepo = slotRepo;
    }

    // ===================== GET ALL APPOINTMENTS =====================
    public List<Appointment> getAllAppointments(int adminId) throws SQLException {

        List<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT 
                a.appointment_id,
                a.account_id,
                u.username,
                a.slot_id,
                a.participants,
                a.status,
                a.type
            FROM appointments a
            JOIN accounts u ON a.account_id = u.account_id
            JOIN appointment_slot s ON a.slot_id = s.slot_id
            WHERE s.account_id = ?
        """;

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Appointment a = new Appointment(
                            rs.getInt("appointment_id"),
                            rs.getInt("account_id"),
                            rs.getInt("slot_id"),
                            rs.getInt("participants"),
                            AppointmentStatus_y.valueOf(rs.getString("status")),
                            AppointmentType_y.valueOf(rs.getString("type"))
                    );

                    a.setUsername(rs.getString("username"));
                    list.add(a);
                }
            }
        }

        return list;
    }

    // ===================== UPCOMING =====================
    public List<Appointment> getUpcomingAppointments() throws SQLException {

        List<Appointment> appointments = new ArrayList<>();

        String sql =
                "SELECT a.appointment_id, a.slot_id, a.account_id, a.participants, a.status, a.type, " +
                "a.start_time, a.end_time " +
                "FROM appointments a " +
                "WHERE a.end_time >= CURRENT_TIMESTAMP";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int appointmentId = rs.getInt("appointment_id");
                int userId = rs.getInt("account_id");
                int slotId = rs.getInt("slot_id");
                int participants = rs.getInt("participants");

                AppointmentStatus_y status =
                        AppointmentStatus_y.valueOf(rs.getString("status"));

                AppointmentType_y type =
                        AppointmentType_y.valueOf(rs.getString("type"));

                ZonedDateTime start =
                        rs.getTimestamp("start_time").toInstant().atZone(ZONE);

                ZonedDateTime end =
                        rs.getTimestamp("end_time").toInstant().atZone(ZONE);

                TimeSlot timeSlot = new TimeSlot(slotId, start, end);

                Appointment appt = new Appointment(
                        appointmentId, userId, slotId,
                        timeSlot, participants, status, type
                );

                appointments.add(appt);
            }
        }

        return appointments;
    }

    // ===================== USER UPCOMING =====================
    public List<Appointment> getUserUpcomingAppointments(int userId) throws SQLException {

        List<Appointment> appointments = new ArrayList<>();

        String sql =
                "SELECT * FROM appointments WHERE account_id = ? ORDER BY start_time ASC";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int slotId = rs.getInt("slot_id");
                int appointmentId = rs.getInt("appointment_id");
                int participants = rs.getInt("participants");

                AppointmentStatus_y status =
                        AppointmentStatus_y.valueOf(rs.getString("status"));

                AppointmentType_y type =
                        AppointmentType_y.valueOf(rs.getString("type"));

                ZonedDateTime start =
                        rs.getTimestamp("start_time").toInstant().atZone(ZONE);

                ZonedDateTime end =
                        rs.getTimestamp("end_time").toInstant().atZone(ZONE);

                TimeSlot timeSlot = new TimeSlot(slotId, start, end);

                if (end.isAfter(ZonedDateTime.now(ZONE))) {
                    appointments.add(new Appointment(
                            appointmentId, userId, slotId,
                            timeSlot, participants, status, type
                    ));
                }
            }
        }

        return appointments;
    }

    // ===================== BOOK =====================
    public boolean book(int userId, int slotId, int participants, AppointmentType_y type) throws SQLException {

        AppointmentSlot_y slot = slotRepo.findById(slotId);

        if (slot == null) {
            throw new IllegalArgumentException("Slot not found");
        }

        ZonedDateTime startZ =
                ZonedDateTime.of(slot.getDate(), slot.getStartTime(), ZONE);

        ZonedDateTime endZ =
                ZonedDateTime.of(slot.getDate(), slot.getEndTime(), ZONE);

        ZonedDateTime now = ZonedDateTime.now(ZONE);

        if (slot.getBookedCount() + participants > slot.getMaxCapacity()) {
            throw new IllegalStateException("No capacity");
        }

        if (startZ.isBefore(now)) return false;
        if (endZ.isBefore(now)) return false;

        long duration = Duration.between(startZ, endZ).toMinutes();

        String sql =
                "INSERT INTO appointments " +
                "(account_id, slot_id, start_time, end_time, duration, participants, status, type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, slotId);
            ps.setTimestamp(3, Timestamp.from(startZ.toInstant()));
            ps.setTimestamp(4, Timestamp.from(endZ.toInstant()));
            ps.setLong(5, duration);
            ps.setInt(6, participants);
            ps.setString(7, AppointmentStatus_y.CONFIRMED.name());
            ps.setString(8, type.name());

            int rows = ps.executeUpdate();

            String updateSlot =
                    "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";

            try (PreparedStatement ps2 = conn.prepareStatement(updateSlot)) {
                ps2.setInt(1, participants);
                ps2.setInt(2, slotId);
                ps2.executeUpdate();
            }

            return rows > 0;
        }
    }

    // ===================== CANCEL =====================
    public boolean cancel(int userId, int appointmentId) throws SQLException {

        String getSql =
                "SELECT slot_id, participants FROM appointments WHERE appointment_id = ? AND account_id = ?";

        String deleteSql =
                "DELETE FROM appointments WHERE appointment_id = ? AND account_id = ?";

        String updateSlot =
                "UPDATE appointment_slot SET booked_count = booked_count - ? WHERE slot_id = ?";

        try (Connection conn = database_connection.getConnection()) {

            conn.setAutoCommit(false);

            int slotId;
            int participants;

            try (PreparedStatement ps = conn.prepareStatement(getSql)) {
                ps.setInt(1, appointmentId);
                ps.setInt(2, userId);

                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                slotId = rs.getInt("slot_id");
                participants = rs.getInt("participants");
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, appointmentId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSlot)) {
                ps.setInt(1, participants);
                ps.setInt(2, slotId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            logger.severe("Cancel error: " + e.getMessage());
            return false;
        }
    }

    // ===================== UPDATE =====================
    public boolean update(int userId, int appointmentId, int participants) throws SQLException {

        String sql =
                "UPDATE appointments SET participants = ? " +
                "WHERE appointment_id = ? AND account_id = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, participants);
            ps.setInt(2, appointmentId);
            ps.setInt(3, userId);

            return ps.executeUpdate() > 0;
        }
    }
}
