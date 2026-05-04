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

/**
 * كلاس مسؤول عن إدارة المواعيد (Appointments) داخل قاعدة البيانات.
 *
 * <p>يوفر هذا الكلاس جميع العمليات المتعلقة بالحجوزات مثل:</p>
 * <ul>
 *   <li>عرض المواعيد (للأدمن أو للمستخدم)</li>
 *   <li>حجز موعد جديد</li>
 *   <li>إلغاء موعد</li>
 *   <li>تحديث عدد المشاركين</li>
 *   <li>حذف موعد</li>
 * </ul>
 *
 * <p>يعتمد على JDBC للتعامل مع قاعدة البيانات، ويستخدم SlotRepository
 * لجلب معلومات الـ TimeSlot.</p>
 *
 * <p>يتم التعامل مع الوقت باستخدام المنطقة الزمنية Asia/Hebron.</p>
 */
public class AppointmentRepository_y {
	private static final String COL_APPOINTMENT_ID = "appointment_id";
	Connection conn = database_connection.getConnection();
    private static final ZoneId ZONE = ZoneId.of("Asia/Hebron");
    private SlotRepository_y slotRepo ;
    /**
     * Constructor افتراضي يقوم بإنشاء SlotRepository داخلي.
     */
    public AppointmentRepository_y() {
        this.slotRepo = new SlotRepository_y();
    }
    /**
     * جلب جميع المواعيد الخاصة بأدمن معين.
     *
     * @param adminId رقم الأدمن
     * @return قائمة بالمواعيد
     * @throws SQLException في حال حدوث خطأ في قاعدة البيانات
     */
    
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
    /**
     * Constructor مع حقن SlotRepository (مفيد للاختبار).
     *
     * @param slotRepo كائن SlotRepository
     */
    public AppointmentRepository_y(SlotRepository_y slotRepo) {
        this.slotRepo = slotRepo;
    }
    
    /**
     * جلب جميع المواعيد القادمة (غير المنتهية).
     *
     * @return قائمة بالمواعيد القادمة
     * @throws SQLException في حال حدوث خطأ
     */
    
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
            	 int appointmentId = rs.getInt(COL_APPOINTMENT_ID );
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

    /**
     * جلب المواعيد القادمة لمستخدم معين.
     *
     * @param userId رقم المستخدم
     * @return قائمة بالمواعيد القادمة الخاصة بالمستخدم
     * @throws SQLException في حال حدوث خطأ
     */
    
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
                int appointmentId = rs.getInt(COL_APPOINTMENT_ID );
                int participants = rs.getInt("participants");
                AppointmentStatus_y status = AppointmentStatus_y.valueOf(rs.getString("status"));
                AppointmentType_y type = AppointmentType_y.valueOf(rs.getString("type"));
                ZonedDateTime start = rs.getTimestamp("start_time").toInstant()
                                        .atZone(ZoneId.of(ZONE));
                ZonedDateTime end = rs.getTimestamp("end_time").toInstant()
                                      .atZone(ZoneId.of(ZONE));

                TimeSlot timeSlot = new TimeSlot(slotId, start, end);
                Appointment appt = new Appointment(appointmentId, userId, slotId, timeSlot, participants, status, type);

                // فلترة المواعيد المنتهية تلقائياً
                if (end.isAfter(ZonedDateTime.now(ZoneId.of(ZONE)))) {
                    appointments.add(appt);
                }
            }
        }

        return appointments;
    }

    /**
     * عرض المواعيد في الكونسول بشكل منسق.
     *
     * @param appointments قائمة المواعيد
     */ 
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
                    rs.getTimestamp("start_time").toInstant().atZone(ZoneId.of(ZONE)),
                    rs.getTimestamp("end_time").toInstant().atZone(ZoneId.of(ZONE))
                );

                return new Appointment(
                    rs.getInt(COL_APPOINTMENT_ID ),
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
  

    public int delete(int id, Connection conn) throws SQLException {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate(); // 🔥
        }
    }
    
    
 // BOOK
    public boolean book(int userId, int slotId, int participants, AppointmentType_y type) throws SQLException {
        // جلب الـ Slot
        AppointmentSlot_y slot = slotRepo.findById(slotId);

        if (slot == null) {
            throw new IllegalArgumentException("Slot with ID " + slotId + " does not exist.");
        }

        // تحويل الوقت إلى ZonedDateTime
        ZonedDateTime startZ = ZonedDateTime.of(slot.getDate(), slot.getStartTime(), ZoneId.of(ZONE));
        ZonedDateTime endZ = ZonedDateTime.of(slot.getDate(), slot.getEndTime(), ZoneId.of(ZONE));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ZONE));
       

        // منع حجز أكثر من السعة
        if (slot.getBookedCount() + participants > slot.getMaxCapacity()) {
            throw new IllegalStateException("Cannot book this slot: not enough capacity.");
        }
        if (startZ.isBefore(now)) {
            System.out.println("❌ Cannot book a slot in the past!");
            return false;
        }if (endZ.isBefore(now)) {
            System.out.println("❌ This slot already ended!");
            return false;
        }
        // حساب المدة بالدقائق
        long duration = Duration.between(startZ, endZ).toMinutes();

        try (Connection conn = database_connection.getConnection()) {
            // تحديد حالة الحجز
            AppointmentStatus_y status = AppointmentStatus_y.CONFIRMED;

            String sql = "INSERT INTO appointments " +
                         "(account_id, slot_id, start_time, end_time, duration, participants, status, type) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, slotId);
                ps.setTimestamp(3, Timestamp.from(startZ.toInstant()));
                ps.setTimestamp(4, Timestamp.from(endZ.toInstant()));
                ps.setLong(5, duration);
                ps.setInt(6, participants);
                ps.setString(7, status.name());
                ps.setString(8, type.name());
                int rows = ps.executeUpdate();

                // تحديث booked_count
                String updateSlot = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(updateSlot)) {
                    ps2.setInt(1, participants);
                    ps2.setInt(2, slotId);
                    ps2.executeUpdate();
                }

                System.out.println("Appointment booked successfully! Status: " + status + ", Type: " + type);
                return rows > 0;
            }
        }
    }
    // CANCEL
    public boolean cancel(int userId, int appointmentId) throws SQLException {

        String getSlotSql =
                "SELECT slot_id, participants FROM appointments WHERE appointment_id = ? AND account_id = ?";

        String deleteSql =
                "DELETE FROM appointments WHERE appointment_id = ? AND account_id = ?";

        String updateSlotSql =
                "UPDATE appointment_slot SET booked_count = booked_count - ? WHERE slot_id = ?";

        try (Connection conn = database_connection.getConnection()) {

            conn.setAutoCommit(false);

            int slotId;
            int participants;

            // 1️⃣ جيب البيانات قبل الحذف
            try (PreparedStatement ps = conn.prepareStatement(getSlotSql)) {
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

            // 2️⃣ احذف الموعد
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, appointmentId);
                ps.setInt(2, userId);

                int rows = ps.executeUpdate();

                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 3️⃣ رجّع الكاباسيتي
            try (PreparedStatement ps = conn.prepareStatement(updateSlotSql)) {
                ps.setInt(1, participants);
                ps.setInt(2, slotId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
        logger.error("Error fetching account from database", e);
            return false;
        }
    }
    public String getUserEmailByAppointment(int appointmentId) throws SQLException {
        String sql = "SELECT email FROM accounts a " +
                     "JOIN appointments ap ON a.account_id = ap.account_id " +
                     "WHERE ap.appointment_id = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("email");
            }
        }

        return null;
    }
    // UPDATE
    public boolean update(int userId, int appointmentId, int participants) throws SQLException {
    	String sql =
    			"UPDATE appointments a " +
    			"JOIN appointment_slot s ON a.slot_id = s.slot_id " +
    			"SET a.participants = ? " +
    			"WHERE a.appointment_id = ? " +
    			"AND a.account_id = ? " +
    			"AND (s.slot_date > CURRENT_DATE " +
    			"OR (s.slot_date = CURRENT_DATE AND s.slot_start_time > CURRENT_TIME))";
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
