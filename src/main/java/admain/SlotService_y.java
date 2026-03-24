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
   // private static Scanner sc = new Scanner(System.in);
    // Dependency Injection
    public SlotService_y(AppointmentRepository_y appointmentRepo,
                         SlotRepository_y slotRepo ,NotificationService_y notificationService) {
        this.appointmentRepo = appointmentRepo;
        this.slotRepo = slotRepo;
        this.notificationService = notificationService;
    }

    /////////////////////////////
    // GET AVAILABLE SLOTS
    public List<AppointmentSlot_y> getAvailableSlots() {
        return slotRepo.findAvailableSlots();
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
  
    public boolean adminCancelAppointment(int appointmentId) throws SQLException {
        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);

            Appointment appointment = appointmentRepo.findById(appointmentId, conn);
            if (appointment == null) {
                conn.rollback();
                return false;
            }

            int userId = appointment.getUserId();
            int slotId = appointment.getSlotId();
            int participants = appointment.getParticipants();

            // 🔹 جيب معلومات السلووت
            AppointmentSlot_y originalSlot = slotRepo.findById(slotId);

            // 🔹 حذف الموعد
            appointmentRepo.delete(appointmentId, conn);

            // 🔹 تحديث الكاباسيتي
            slotRepo.decreaseBookedCount(slotId, participants, conn);

            conn.commit();

            // 🔹 إشعار الإلغاء
            notificationService.sendNotification(
                userId,
                "⚠️ Your appointment was cancelled by admin."
            );

            // 🔥 اقتراح بدائل
            if (originalSlot != null) {

                List<AppointmentSlot_y> alternatives =
                    slotRepo.findAvailableSlotsByDate(originalSlot.getDate());

                // نحذف نفس السلووت
                alternatives.removeIf(s -> s.getId() == slotId);

                if (!alternatives.isEmpty()) {

                    String msg = "📅 Available alternatives:\n";

                    for (AppointmentSlot_y s : alternatives) {
                        msg += "ID: " + s.getId() +
                               " | " + s.getStartTime() +
                               " - " + s.getEndTime() + "\n";
                    }

                    notificationService.sendNotification(userId, msg);

                } else {
                    notificationService.sendNotification(
                        userId,
                        "❗ No alternative slots available."
                    );
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // ---------------- Cancel Entire Slot ----------------
    public boolean adminCancelSlot(int slotId) {
        // نجيب كل المستخدمين اللي عندهم مواعيد في هذا الـ Slot
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
                    userIds.add(rs.getInt("account_id")); // صححنا هنا
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
    }}