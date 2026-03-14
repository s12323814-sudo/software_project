package admain;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Scanner;

import org.mindrot.jbcrypt.BCrypt;

public class UserMenu_y {

    private static SlotService_y slotService = new SlotService_y();
    private static Scanner sc = new Scanner(System.in);

    public static void showMenu() {

        while (true) {
            System.out.println("\n=== User Menu ===");
            System.out.println("1- Login");
            System.out.println("2- Register");
            System.out.println("3- forgot Password");
            System.out.println("4- Exit");

            int choice;

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {

                case 1:
                    loginUser();
                    break;

                case 2:
                    registerUser();
                    break;

                case 3:
                    forgotPassword();
                    break;

                case 4:
                    System.out.println("Exiting...");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void forgotPassword() {

        System.out.print("Enter your email: ");
        String email = sc.nextLine();

        if (!login_foruser_y.emailExists(email)) {
            System.out.println("Email not found!");
            return;
        }

        String otp = OTPGenerator_y.generateOTP();

        EmailService_y emailService = new MockEmailService_y();
        emailService.sendOTP(email, otp);

        System.out.print("Enter OTP sent to your email: ");
        String userOtp = sc.nextLine();

        if (otp.equals(userOtp)) {

            System.out.print("Enter new password: ");
            String newPassword = sc.nextLine();

            if (login_foruser_y.updatePassword(email, newPassword)) {
                System.out.println("Password updated successfully!");
            }

        } else {
            System.out.println("Wrong OTP.");
        }
    }

    private static void loginUser() {

        System.out.print("Username: ");
        String username = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        users_y user = login_foruser_y.login(username, password);

        if (user != null) {

            session_y.currentUser = user;

            System.out.println("Login Successful!");

            userSession();

        } else {
            System.out.println("Login Failed! Check your username/password.");
        }
    }

    private static void registerUser() {

        System.out.print("New Username: ");
        String username = sc.nextLine();

        System.out.print("New Password: ");
        String password = sc.nextLine();

        
        if (password.length() < 8) {
            System.out.println("Password must be at least 8 characters.");
            return;
        }

        System.out.print("Email: ");
        String email = sc.nextLine();

       
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        if (login_foruser_y.register(username, hashedPassword, email)) {

            System.out.println("Account created successfully!");

        } else {

            System.out.println("Registration failed! Username or email may already exist.");
        }
    }

    private static void userSession() {

        while (session_y.currentUser != null) {

            System.out.println("\nWelcome " + session_y.currentUser.getUsername());
            System.out.println("1- View Available Slots");
            System.out.println("2- View My Appointments");
            System.out.println("3- Book Appointment");
            System.out.println("4- Update Appointment"); // ← تم إضافة الخيار الجديد
            System.out.println("5- Cancel Appointment");
            System.out.println("6- Logout");
            int choice;

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {

                case 1:
                    viewAvailableSlots();
                    break;
               
                case 2:
                	viewUserAppointments(); break;
                case 3:
                    bookAppointment();
                    break;
                case 4:
                	updateAppointment();
                    break;
                case 5:
                	cancelAppointment();
                    break;
                case 6:
                    session_y.logoutUser();
                    break;

                default:
                    System.out.println("Invalid choice.");
            
        }}}
        private static void viewUserAppointments() {
            if (session_y.currentUser == null) {
                System.out.println("Please login first!");
                return;
            }

            String sql = "SELECT appointment_id, start_time, end_time, participants, status " +
                         "FROM appointments " +
                         "WHERE user_id = ? " +
                         "ORDER BY start_time";

            try (Connection conn = database_connection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, session_y.currentUser.getUserId());

                try (ResultSet rs = ps.executeQuery()) {
                    System.out.printf("%-5s %-20s %-20s %-12s %-10s%n",
                                      "ID", "Start Time", "End Time", "Participants", "Status");

                    boolean hasAppointments = false;

                    while (rs.next()) {
                        hasAppointments = true;
                        int id = rs.getInt("appointment_id");
                        Timestamp start = rs.getTimestamp("start_time");
                        Timestamp end = rs.getTimestamp("end_time");
                        int participants = rs.getInt("participants");
                        String status = rs.getString("status");

                        System.out.printf("%-5d %-20s %-20s %-12d %-10s%n",
                                          id, start.toString(), end.toString(),
                                          participants, status);
                    }

                    if (!hasAppointments) {
                        System.out.println("No upcoming appointments.");
                    }
                }

            } catch (SQLException e) {
                System.out.println("Error fetching appointments: " + e.getMessage());
            }
        }
        private static void updateAppointment() {
            if (session_y.currentUser == null) {
                System.out.println("Please login first!");
                return;
            }

            try {
                System.out.print("Enter Appointment ID to update: ");
                int appointmentId = Integer.parseInt(sc.nextLine());

                // جلب معلومات الموعد الحالي
                AppointmentService apptService = new AppointmentService();
                List<Appointment> userAppointments = apptService.getUserAppointments(session_y.currentUser.getUserId());
                Appointment currentAppt = null;
                for (Appointment a : userAppointments) {
                    if (a.getTimeSlot().getId() == appointmentId) {
                        currentAppt = a;
                        break;
                    }
                }

                if (currentAppt == null) {
                    System.out.println("Appointment not found or cannot be modified.");
                    return;
                }

                if (currentAppt.getTimeSlot().getStart().toLocalDateTime().isBefore(java.time.LocalDateTime.now())) {
                    System.out.println("Cannot update past appointments.");
                    return;
                }

                // عرض كل الـ Slots المتاحة قبل فتح Connection
                List<AppointmentSlot_y> slots = apptService.getAvailableSlots();
                System.out.println("\nAvailable Slots:");
                for (AppointmentSlot_y slot : slots) {
                    int remaining = slot.getMaxCapacity() - slot.getBookedCount();
                    System.out.printf("ID: %d | Date: %s | Start: %s | End: %s | Capacity: %d | Booked: %d | Remaining: %d%n",
                            slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(),
                            slot.getMaxCapacity(), slot.getBookedCount(), remaining);
                }

                System.out.print("Enter new Slot ID: ");
                int newSlotId = Integer.parseInt(sc.nextLine());

                AppointmentSlot_y newSlot = null;
                for (AppointmentSlot_y slot : slots) {
                    if (slot.getId() == newSlotId) {
                        newSlot = slot;
                        break;
                    }
                }
                if (newSlot == null) {
                    System.out.println("Slot not found.");
                    return;
                }

                int remaining = newSlot.getMaxCapacity() - newSlot.getBookedCount();
                System.out.print("Enter number of participants: ");
                int newParticipants = Integer.parseInt(sc.nextLine());

                if (newParticipants > remaining && currentAppt.getTimeSlot().getId() != newSlotId) {
                    System.out.println("Cannot book more than remaining capacity (" + remaining + ").");
                    return;
                }

                // فتح Connection لتعديل appointments و booked_count
                try (Connection conn = database_connection.getConnection()) {
                    conn.setAutoCommit(false);

                    int oldSlotId = currentAppt.getTimeSlot().getId();
                    int oldParticipants = currentAppt.getParticipants();

                    // تعديل booked_count للسلوط القديم والجديد
                    if (oldSlotId != newSlotId) {
                        String updateOldSlot = "UPDATE appointment_slot SET booked_count = booked_count - ? WHERE slot_id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateOldSlot)) {
                            ps.setInt(1, oldParticipants);
                            ps.setInt(2, oldSlotId);
                            ps.executeUpdate();
                        }

                        String updateNewSlot = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateNewSlot)) {
                            ps.setInt(1, newParticipants);
                            ps.setInt(2, newSlotId);
                            ps.executeUpdate();
                        }
                    } else {
                        int diff = newParticipants - oldParticipants;
                        String updateSlot = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateSlot)) {
                            ps.setInt(1, diff);
                            ps.setInt(2, oldSlotId);
                            ps.executeUpdate();
                        }
                    }

                    // تحديث appointments
                    String updateAppt = "UPDATE appointments SET slot_id = ?, participants = ? WHERE appointment_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateAppt)) {
                        ps.setInt(1, newSlotId);
                        ps.setInt(2, newParticipants);
                        ps.setInt(3, appointmentId);
                        ps.executeUpdate();
                    }

                    conn.commit();
                    System.out.println("Appointment updated successfully!");
                } catch (SQLException e) {
                    System.out.println("Error updating appointment: " + e.getMessage());
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }





    private static void viewAvailableSlots() {

        List<AppointmentSlot_y> slots = slotService.getAvailableSlots();

        if (slots.isEmpty()) {

            System.out.println("No available slots.");

            return;
        }

        System.out.println("\nAvailable Slots:");

        System.out.printf("%-5s %-20s %-20s %-10s %-10s %-10s%n",
                "ID", "Start Time", "End Time", "Capacity", "Booked", "Remaining");

        for (AppointmentSlot_y slot : slots) {

            int remaining = slot.getMaxCapacity() - slot.getBookedCount();

            String startTime = slot.getDate() + " " + slot.getStartTime();
            String endTime = slot.getDate() + " " + slot.getEndTime();

            if (remaining > 0) {

                System.out.printf("%-5d %-20s %-20s %-10d %-10d %-10d%n",
                        slot.getId(),
                        startTime,
                        endTime,
                        slot.getMaxCapacity(),
                        slot.getBookedCount(),
                        remaining
                );
            }
        }
    }
    private static void cancelAppointment() {
        if (session_y.currentUser == null) {
            System.out.println("Please login first!");
            return;
        }

        try {
            System.out.print("Enter Appointment ID to cancel: ");
            int appointmentId = Integer.parseInt(sc.nextLine());

            try (java.sql.Connection conn = database_connection.getConnection()) {
                conn.setAutoCommit(false);

                // جلب معلومات الموعد للتحقق من المالك والوقت
                String selectSql = "SELECT slot_id, participants, start_time FROM appointments WHERE appointment_id = ? AND user_id = ?";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setInt(1, appointmentId);
                    ps.setInt(2, session_y.currentUser.getUserId());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("Appointment not found or cannot be canceled.");
                            return;
                        }

                        int slotId = rs.getInt("slot_id");
                        int participants = rs.getInt("participants");
                        java.sql.Timestamp startTime = rs.getTimestamp("start_time");
                      
                        java.time.LocalDateTime appointmentDateTime = startTime.toLocalDateTime();
                        java.time.LocalDateTime now = java.time.LocalDateTime.now();
                        if (appointmentDateTime.isBefore(now)) {
                            System.out.println("Cannot cancel past appointments.");
                            return;
                        }
                        
                        // حذف الموعد
                        String deleteSql = "DELETE FROM appointments WHERE appointment_id = ?";
                        try (java.sql.PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                            deletePs.setInt(1, appointmentId);
                            deletePs.executeUpdate();
                        }

                        // تحديث booked_count في slot
                        String updateSql = "UPDATE appointment_slot SET booked_count = booked_count - ? WHERE slot_id = ?";
                        try (java.sql.PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                            updatePs.setInt(1, participants);
                            updatePs.setInt(2, slotId);
                            updatePs.executeUpdate();
                        }

                        conn.commit();
                        System.out.println("Appointment canceled successfully!");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    private static void bookAppointment() {
        if (session_y.currentUser == null) {
            System.out.println("Please login first!");
            return;
        }

        try {
            System.out.print("Enter Slot ID to book: ");
            int slotId = Integer.parseInt(sc.nextLine());

            System.out.print("Enter Number of Participants: ");
            int participants = Integer.parseInt(sc.nextLine());

            // فتح اتصال بالـ DB
            try (Connection conn = database_connection.getConnection()) {
                conn.setAutoCommit(false);

                // جلب بيانات الـ slot
                String sql = "SELECT slot_date, slot_start_time, slot_end_time, max_capacity, booked_count " +
                             "FROM appointment_slot WHERE slot_id = ? FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, slotId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("Slot not found.");
                            return;
                        }

                        // تحويل التاريخ والوقت إلى LocalDateTime
                        LocalDateTime start = LocalDateTime.of(
                            rs.getDate("slot_date").toLocalDate(),
                            rs.getTime("slot_start_time").toLocalTime()
                        );
                        LocalDateTime end = LocalDateTime.of(
                            rs.getDate("slot_date").toLocalDate(),
                            rs.getTime("slot_end_time").toLocalTime()
                        );

                        int capacity = rs.getInt("max_capacity");
                        int booked = rs.getInt("booked_count");

                        // منع الحجز بالماضي
                        if (start.isBefore(LocalDateTime.now())) {
                            System.out.println("Cannot book a past slot.");
                            return;
                        }

                        int remaining = capacity - booked;
                        if (participants > remaining) {
                            System.out.println("Not enough capacity. Remaining: " + remaining);
                            return;
                        }

                        long duration = Duration.between(start, end).toMinutes();
                        if (duration < 30 || duration > 120) {
                            System.out.println("Slot duration must be between 30 and 120 minutes.");
                            return;
                        }

                        // إدخال الحجز
                        String insert = "INSERT INTO appointments " +
                                        "(user_id, slot_id, start_time, end_time, duration, participants, status) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement psInsert = conn.prepareStatement(insert)) {
                            psInsert.setInt(1, session_y.currentUser.getUserId());
                            psInsert.setInt(2, slotId);
                            psInsert.setTimestamp(3, Timestamp.valueOf(start));
                            psInsert.setTimestamp(4, Timestamp.valueOf(end));
                            psInsert.setInt(5, (int) duration);
                            psInsert.setInt(6, participants);
                            psInsert.setString(7, "CONFIRMED");
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

                        System.out.println("Appointment booked successfully!");
                        System.out.println("Remaining capacity: " + (remaining - participants));
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Booking failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}