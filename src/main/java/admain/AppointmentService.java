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

    private SlotService_y slotService;
    private scheduleRepository repo;

    private static final int MIN_DURATION = 30;
    private static final int MAX_DURATION = 120;

    private Connection testConnection = null;

    // Constructor للتست
    public AppointmentService(Connection conn,
                              SlotService_y slotService,
                              scheduleRepository repo) {
        this.testConnection = conn;
        this.slotService = slotService;
        this.repo = repo;
    }

    // Default constructor
    public AppointmentService() {
        this.repo = new scheduleRepository();
        this.slotService = new SlotService_y();
    }

    public void bookAppointment(int userId,
                                int slotId,
                                int participants,
                                AppointmentType_y type) throws SQLException {

        Connection conn = (testConnection != null)
                ? testConnection
                : database_connection.getConnection();

        try {
            conn.setAutoCommit(false);

            String sql =
                    "SELECT slot_date AS start_date, slot_start_time AS start_time, " +
                    "slot_end_time AS end_time, max_capacity, booked_count " +
                    "FROM appointment_slot WHERE slot_id = ? FOR UPDATE";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, slotId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new SQLException("Slot not found");
                    }

                    java.sql.Date sqlDate = rs.getDate("start_date");
                    java.sql.Time startTime = rs.getTime("start_time");
                    java.sql.Time endTime = rs.getTime("end_time");

                    if (sqlDate == null || startTime == null || endTime == null) {
                        throw new SQLException("Invalid slot data");
                    }

                    LocalDateTime start =
                            sqlDate.toLocalDate().atTime(startTime.toLocalTime());

                    LocalDateTime end =
                            sqlDate.toLocalDate().atTime(endTime.toLocalTime());

                    if (start.isBefore(LocalDateTime.now())) {
                        throw new SQLException("Cannot book a past slot");
                    }

                    int capacity = rs.getInt("max_capacity");
                    int booked = rs.getInt("booked_count");

                    int remaining = capacity - booked;

                    if (participants > remaining) {
                        throw new SQLException("Not enough capacity");
                    }

                    long duration = Duration.between(start, end).toMinutes();

                    if (duration < MIN_DURATION || duration > MAX_DURATION) {
                        throw new IllegalArgumentException(
                                "Duration must be between 30 and 120 minutes.");
                    }

                    String insert =
                            "INSERT INTO appointments " +
                            "(account_id, slot_id, start_time, end_time, " +
                            "duration, participants, status, type) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement psInsert =
                                 conn.prepareStatement(insert)) {

                        psInsert.setInt(1, userId);
                        psInsert.setInt(2, slotId);
                        psInsert.setTimestamp(3, Timestamp.valueOf(start));
                        psInsert.setTimestamp(4, Timestamp.valueOf(end));
                        psInsert.setInt(5, (int) duration);
                        psInsert.setInt(6, participants);
                        psInsert.setString(
                                7,
                                AppointmentStatus_y.CONFIRMED.name());

                        psInsert.setString(8, type.name());

                        psInsert.executeUpdate();
                    }

                    String update =
                            "UPDATE appointment_slot " +
                            "SET booked_count = booked_count + ? " +
                            "WHERE slot_id = ?";

                    try (PreparedStatement psUpdate =
                                 conn.prepareStatement(update)) {

                        psUpdate.setInt(1, participants);
                        psUpdate.setInt(2, slotId);

                        psUpdate.executeUpdate();
                    }

                    conn.commit();
                }
            }

        } catch (SQLException | IllegalArgumentException e) {

            conn.rollback();
            throw e;

        } finally {

            if (testConnection == null) {
                conn.close();
            }
        }
    }

    public List<Appointment> getUserAppointments(int userId)
            throws SQLException {

        return repo.getAppointments(userId);
    }

    public List<AppointmentSlot_y> getAvailableSlots() {
        return slotService.getAvailableSlots();
    }

    public void addSlot(LocalDateTime start,
                        LocalDateTime end,
                        int capacity,
                        int adminId) {

        slotService.addSlot(
                start.toLocalDate(),
                start.toLocalTime(),
                end.toLocalTime(),
                capacity,
                adminId);
    }
}
