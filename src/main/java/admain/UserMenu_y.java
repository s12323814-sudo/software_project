package admain;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

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

        System.out.print("Email: ");
        String email = sc.nextLine();

        if (login_foruser_y.register(username, password, email)) {

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
            }
        }
    }private static void viewUserAppointments() {
        if (session_y.currentUser == null) {
            System.out.println("Please login first!");
            return;
        }

        String sql = "SELECT a.appointment_id, s.slot_date, s.slot_start_time, s.slot_end_time, " +
                     "a.participants, a.status " +
                     "FROM appointments a " +
                     "JOIN appointment_slot s ON a.slot_id = s.slot_id " +
                     "WHERE a.user_id = ? " +
                     "ORDER BY s.slot_date, s.slot_start_time";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, session_y.currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-12s %-10s %-10s %-12s %-10s%n",
                                  "ID", "Date", "Start", "End", "Participants", "Status");
                boolean hasAppointments = false;
                while (rs.next()) {
                    hasAppointments = true;
                    int id = rs.getInt("appointment_id");
                    Date date = rs.getDate("slot_date");
                    Time start = rs.getTime("slot_start_time");
                    Time end = rs.getTime("slot_end_time");
                    int participants = rs.getInt("participants");
                    String status = rs.getString("status");

                    System.out.printf("%-5d %-12s %-10s %-10s %-12d %-10s%n",
                                      id, date.toString(), start.toString(), end.toString(),
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

        try (Connection conn = database_connection.getConnection()) {
            System.out.print("Enter Appointment ID to update: ");
            int appointmentId = Integer.parseInt(sc.nextLine());

            // جلب معلومات الموعد الحالي
            String selectSql = "SELECT slot_id, participants, start_time FROM appointments WHERE appointment_id = ? AND user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, appointmentId);
                ps.setInt(2, session_y.currentUser.getUserId());

                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("Appointment not found or cannot be modified.");
                    return;
                }

                int oldSlotId = rs.getInt("slot_id");
                int oldParticipants = rs.getInt("participants");
                java.sql.Timestamp startTime = rs.getTimestamp("start_time");

                if (startTime.toLocalDateTime().isBefore(java.time.LocalDateTime.now())) {
                    System.out.println("Cannot update past appointments.");
                    return;
                }

                // عرض السلوطات المتاحة
                List<AppointmentSlot_y> slots = slotService.getAvailableSlots();
                System.out.println("\nAvailable Slots:");
                for (AppointmentSlot_y slot : slots) {
                    int remaining = slot.getMaxCapacity() - slot.getBookedCount();
                    System.out.printf("ID: %d | Date: %s | Start: %s | End: %s | Remaining: %d%n",
                            slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(), remaining);
                }

                // اختيار سلوط جديد
                System.out.print("Enter new Slot ID: ");
                int newSlotId = Integer.parseInt(sc.nextLine());
                AppointmentSlot_y newSlot = slotService.getSlotById(newSlotId);
                if (newSlot == null) {
                    System.out.println("Slot not found.");
                    return;
                }

                int remaining = newSlot.getMaxCapacity() - newSlot.getBookedCount();
                if (oldSlotId != newSlotId) { // إذا غير السلوط، نتحقق من السعة
                    if (remaining <= 0) {
                        System.out.println("This slot is fully booked. Cannot update.");
                        return;
                    }
                }

                System.out.print("Enter number of participants: ");
                int newParticipants = Integer.parseInt(sc.nextLine());
                if (newParticipants > remaining && oldSlotId != newSlotId) {
                    System.out.println("Cannot book more than remaining capacity (" + remaining + ").");
                    return;
                }

                conn.setAutoCommit(false);

                // إعادة تحديث booked_count للسلوط القديم (إذا غير السلوط)
                if (oldSlotId != newSlotId) {
                    String updateOldSlot = "UPDATE appointment_slot SET booked_count = booked_count - ? WHERE slot_id = ?";
                    try (PreparedStatement updOld = conn.prepareStatement(updateOldSlot)) {
                        updOld.setInt(1, oldParticipants);
                        updOld.setInt(2, oldSlotId);
                        updOld.executeUpdate();
                    }

                    String updateNewSlot = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                    try (PreparedStatement updNew = conn.prepareStatement(updateNewSlot)) {
                        updNew.setInt(1, newParticipants);
                        updNew.setInt(2, newSlotId);
                        updNew.executeUpdate();
                    }
                } else { // إذا نفس السلوط فقط نحدث عدد المحجوزين
                    int diff = newParticipants - oldParticipants;
                    String updateSlot = "UPDATE appointment_slot SET booked_count = booked_count + ? WHERE slot_id = ?";
                    try (PreparedStatement upd = conn.prepareStatement(updateSlot)) {
                        upd.setInt(1, diff);
                        upd.setInt(2, oldSlotId);
                        upd.executeUpdate();
                    }
                }

                // تحديث appointments
                String updateAppt = "UPDATE appointments SET slot_id = ?, participants = ? WHERE appointment_id = ?";
                try (PreparedStatement updAppt = conn.prepareStatement(updateAppt)) {
                    updAppt.setInt(1, newSlotId);
                    updAppt.setInt(2, newParticipants);
                    updAppt.setInt(3, appointmentId);
                    updAppt.executeUpdate();
                }

                conn.commit();
                System.out.println("Appointment updated successfully!");

            } catch (Exception e) {
                conn.rollback();
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

            AppointmentSlot_y slot = slotService.getSlotById(slotId);

            if (slot == null) {

                System.out.println("Slot not found.");

                return;
            }

            int remaining = slot.getMaxCapacity() - slot.getBookedCount();

            if (remaining <= 0) {

                System.out.println("This slot is fully booked.");

                return;
            }

            System.out.print("Enter Number of Participants: ");

            int participants = Integer.parseInt(sc.nextLine());

            if (participants > remaining) {

                System.out.println("Cannot book more than remaining capacity (" + remaining + ").");

                System.out.println("You can book up to " + remaining + " participants for this slot.");

                return;
            }

            slotService.bookSlotForUser(session_y.currentUser.getUserId(), slotId, participants);

            int newRemaining = remaining - participants;

            System.out.println("Appointment booked successfully!");

            System.out.println("Remaining capacity: " + newRemaining);

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }
}