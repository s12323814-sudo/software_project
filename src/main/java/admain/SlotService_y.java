package admain;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class SlotService_y {

    private NotificationService_y notificationService;
    private AppointmentRepository_y appointmentRepo;
    private SlotRepository_y slotRepo;
    private EmailService_y emailService;
    // private static Scanner sc = new Scanner(System.in);

    // Dependency Injection
    public SlotService_y(AppointmentRepository_y appointmentRepo,
            SlotRepository_y slotRepo,
            NotificationService_y notificationService,
            EmailService_y emailService) {

this.appointmentRepo = appointmentRepo;
this.slotRepo = slotRepo;
this.notificationService = notificationService;
this.emailService = emailService; // ✅ الآن صح
}

    /////////////////////////////
    // GET AVAILABLE SLOTS
    public List<AppointmentSlot_y> getAvailableSlots() {
        return slotRepo.findAvailableSlots();
    }
    public List<Appointment> getAllAppointments() throws SQLException {
        return appointmentRepo.getAllAppointments();
    }
    /////////////////////////////
    // BOOK APPOINTMENT
    public boolean bookAppointment(int userId, int slotId, int participants, AppointmentType_y type) throws SQLException {

        AppointmentSlot_y slot = slotRepo.findById(slotId);
        if (slot == null) return false;

        int remaining = slot.getMaxCapacity() - slot.getBookedCount();

        // 🔥 RULES حسب النوع
        switch (type) {

            case URGENT:
            case INDIVIDUAL:
                if (participants != 1) {
                    System.out.println("This type allows only 1 participant.");
                    return false;
                }
                break;

            case GROUP:
                if (participants < 2) {
                    System.out.println("Group must have at least 2 participants.");
                    return false;
                }
                break;

            case VIRTUAL:
                // مثال: ما نهتم بالcapacity
                remaining = Integer.MAX_VALUE;
                break;

            default:
                // GENERAL, FOLLOW_UP, ASSESSMENT
                break;
        }

        if (participants > remaining) {
            System.out.println("Not enough capacity.");
            return false;
        }

        return appointmentRepo.book(userId, slotId, participants, type);
    }

    /////////////////////////////
    // CANCEL (USER)
    public boolean cancelAppointment(int userId, int appointmentId) throws SQLException {
        return appointmentRepo.cancel(userId, appointmentId);
    }

    /////////////////////////////
    // UPDATE
    public boolean updateAppointment(int userId, int appointmentId, int participants) throws SQLException {
        return appointmentRepo.update(userId, appointmentId, participants);
    }

    /////////////////////////////
    // VIEW USER APPOINTMENTS
    public List<Appointment> viewUserAppointments(int userId) throws SQLException {
        return appointmentRepo.getUserUpcomingAppointments(userId);
    }

    /////////////////////////////
    // ADMIN: ADD SLOT
    public boolean addSlot(LocalDate date, LocalTime start, LocalTime end,
                           int capacity, int adminId) {

        if (capacity <= 0) return false;

        return slotRepo.addSlot(date, start, end, capacity, adminId);
    }

    /////////////////////////////
    // ADMIN: CANCEL WITH TRANSACTION (IMPORTANT)

    // ---------------- Cancel Entire Slot ----------------
    public boolean adminCancelSlot(int slotId) {

        String getUsers = "SELECT account_id FROM appointments WHERE slot_id = ?";
        String deleteAppointments = "DELETE FROM appointments WHERE slot_id = ?";
        String deleteSlot = "DELETE FROM appointment_slot WHERE slot_id = ?";

        List<Integer> userIds = new ArrayList<>();

        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);

            // جمع جميع المستخدمين
            try (PreparedStatement psUsers = conn.prepareStatement(getUsers)) {
                psUsers.setInt(1, slotId);
                ResultSet rs = psUsers.executeQuery();
                while (rs.next()) {
                    userIds.add(rs.getInt("account_id"));
                }
            }

            // حذف المواعيد المرتبطة
            try (PreparedStatement psDelAppt = conn.prepareStatement(deleteAppointments)) {
                psDelAppt.setInt(1, slotId);
                int apptsDeleted = psDelAppt.executeUpdate();
                System.out.println("Appointments deleted: " + apptsDeleted);
            }

            // حذف الـ Slot نفسه
            int slotDeleted = 0;
            try (PreparedStatement psDelSlot = conn.prepareStatement(deleteSlot)) {
                psDelSlot.setInt(1, slotId);
                slotDeleted = psDelSlot.executeUpdate();
            }

            if (slotDeleted > 0) {
                conn.commit();

                // إشعار جميع المستخدمين
                for (Integer userId : userIds) {
                    notificationService.sendNotification(
                        userId,
                        "⚠️ Your appointment in slot ID " + slotId + " was cancelled by admin."
                    );
                }

                System.out.println("✅ Slot and its appointments cancelled successfully and users notified!");
                return true;

            } else {
                conn.rollback();
                System.out.println("❌ Failed to delete slot!");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean adminCancelAppointment(int appointmentId) {

        String sql = """
            SELECT a.account_id, u.email
            FROM appointments a
            JOIN accounts u ON a.account_id = u.account_id
            WHERE a.appointment_id = ?
        """;

        String deleteSql = "DELETE FROM appointments WHERE appointment_id = ?";

        try (Connection conn = database_connection.getConnection()) {

            conn.setAutoCommit(false);

            int userId = -1;
            String email = null;

            // 1️⃣ جلب البيانات (user + email)
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appointmentId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    userId = rs.getInt("account_id");
                    email = rs.getString("email");
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // 2️⃣ حذف الموعد
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, appointmentId);

                int deleted = ps.executeUpdate();
                if (deleted == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 3️⃣ تأكيد العملية
            conn.commit();

            // 4️⃣ إشعار داخل النظام
            notificationService.sendNotification(
                userId,
                "⚠️ Your appointment was cancelled by admin."
            );

            // 5️⃣ إرسال الإيميل الحقيقي
            if (email != null && !email.isEmpty()) {
                emailService.sendEmail(
                    email,
                    "Appointment Cancelled",
                    "Dear user,\n\nYour appointment has been cancelled by admin.\n\nRegards."
                );
            }

            System.out.println("✅ Appointment cancelled + Email sent!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }}