package admain;


import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class Main {
	 static AppointmentRepository_y appointmentRepository = new AppointmentRepository_y();
    private static Scanner sc = new Scanner(System.in);
    private static AppointmentRepository_y appointmentRepo = new AppointmentRepository_y();
    private static SlotRepository_y slotRepo = new SlotRepository_y();

    private static SlotService_y slotService = new SlotService_y(appointmentRepo, slotRepo);
    private static authService_y authService = new authService_y();
    private static session_y session;
    private static Account_y user;
    private static NotificationService_y notificationService = new MockNotificationService_y();
    private static session_y currentSession = null;
    private static ReminderManager_y reminderManager =
            new ReminderManager_y(appointmentRepository, notificationService);
    public static void main(String[] args) {
    	 ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	        scheduler.scheduleAtFixedRate(() -> {
	            if (currentSession != null) {
	                reminderManager.checkReminders();
	            }
	        }, 0, 1, TimeUnit.MINUTES);

        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1- Login as User/Admin");
            System.out.println("2- Register");
            System.out.println("3- Forgot Password");
            System.out.println("4- Exit");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid input!");
                continue;
            }

            switch (choice) {
                case 1:
                    loginMenu();
                    break;
                case 2:
                    registerMenu();
                    break;
                case 3:
                    forgotPasswordMenu();
                    break;
                case 4:
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice");
            }
        }
    }
    private static void loginMenu() {
        System.out.print("Username or Email: ");
        String input = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        session = authService.login(input, password);
        if (session == null) {
            System.out.println("Login failed!");
            return;
        }

        // تعيين الـ user من الـ session
        user = session.getAccount();
        
        // تعيين الـ user في الجلسة العامة
        if (session.isAdmin()) {
            session_y.currentAdmin = user;
        } else {
            session_y.currentUser = user;
        }

        System.out.println("Login successful! Role: " + user.getRole());

        if (session.isAdmin()) adminSession();
        else userSession();
    }

    private static void registerMenu() {
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Role (USER/ADMIN): ");
        String role = sc.nextLine().trim().toUpperCase();
        Account_y acc = authService.register(username, password, email, role);
        if (acc != null) System.out.println("Account created successfully!");
        else System.out.println("Registration failed!");
    }

    private static void forgotPasswordMenu() {
        System.out.print("Enter your email: ");
        String email = sc.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine();

        if (authService.updatePassword(email, newPassword))
            System.out.println("Password updated successfully!");
        else
            System.out.println("Failed to update password!");
    }

    // -------------------- User Menu --------------------
    private static void userSession() {
        while (session != null && session.isUser()) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1- View Available Slots");
            System.out.println("2- View My Appointments");
            System.out.println("3- Book Appointment");
            System.out.println("4- Update Appointment");
            System.out.println("5- Cancel Appointment");
            System.out.println("6- Logout");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (Exception e) { choice = -1; }

            switch (choice) {
                case 1: viewAvailableSlots(); break;
                case 2: viewUserAppointments(); break;
                case 3: bookAppointment(); break;
                case 4: updateAppointment(); break;
                case 5: adminCancelAppointment(); break;
                case 6: session_y.logoutUser(); session = null; break;
                default: System.out.println("Invalid choice"); 
            }
        }
    }

    // -------------------- Admin Menu --------------------
    private static void adminSession() {
        while (session != null && session.isAdmin()) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1- View Slots");
            System.out.println("2- Add Slot");
            System.out.println("3- Cancel slot");
            System.out.println("4- Cancel Appointment");
            System.out.println("5- Logout");

            int choice;

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid input! Enter a number.");
                continue; 
            }
            switch(choice) {
                case 1: viewAvailableSlots(); break;
                case 2: addSlot(); break;
                case 3:
                    System.out.print("Enter Slot ID to cancel: ");
                    int slotId = Integer.parseInt(sc.nextLine());
                    slotService.adminCancelSlot(slotId); // نادى على الدالة الجديدة
                    break;
                case 4: 
					adminCancelAppointment();
				 break;
                case 5: session_y.logoutAdmin();    return;
                default: System.out.println("Invalid choice");
            }
        }
    }
    public boolean adminCancelSlot(int slotId) throws SQLException {
        String selectSql = "SELECT * FROM appointment_slot WHERE slot_id = ?";
        String deleteSql = "DELETE FROM appointment_slot WHERE slot_id = ?";

        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);

            // تحقق من وجود الـ Slot
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, slotId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("Slot not found.");
                    return false;
                }
            }

            // احذف الـ Slot (وبالتالي كل المواعيد المرتبطة فيه)
            try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                psDelete.setInt(1, slotId);
                psDelete.executeUpdate();
            }

            conn.commit();
            System.out.println("Slot and all its appointments cancelled successfully!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // -------------------- Methods for Users & Admins --------------------
    private static void viewAvailableSlots() {
        try {
            List<AppointmentSlot_y> slots = slotService.getAvailableSlots();
            if (slots.isEmpty()) { 
                System.out.println("No available slots."); 
                return; 
            }

            System.out.printf("%-5s %-12s %-10s %-10s %-10s %-10s %-10s%n",
                    "ID", "Date", "Start", "End", "Capacity", "Remaining", "Status");

            for (AppointmentSlot_y slot : slots) {
                int remaining = slot.getMaxCapacity() - slot.getBookedCount();

                // تحديد الحالة باستخدام الدالة الجاهزة
                ZonedDateTime startZ = ZonedDateTime.of(slot.getDate(), slot.getStartTime(), ZoneId.of("Asia/Hebron"));
                ZonedDateTime endZ = ZonedDateTime.of(slot.getDate(), slot.getEndTime(), ZoneId.of("Asia/Hebron"));

                // نفرض participant = 1 للعرض
                AppointmentStatus_y status = scheduleRepository.determineStatus(startZ, endZ, 1, slot);

                System.out.printf("%-5d %-12s %-10s %-10s %-10d %-10d %-10s%n",
                        slot.getId(), slot.getDate(), slot.getStartTime(),
                        slot.getEndTime(), slot.getMaxCapacity(), remaining, status);
            }

        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void viewUserAppointments() {
        try {
            List<Appointment> list = slotService.viewUserAppointments(user.getAccountId());
            if (list.isEmpty()) {
                System.out.println("No appointments found.");
            } else {
                list.forEach(System.out::println);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
   
    private static void bookAppointment() {
        try {
            System.out.print("Enter slot ID: ");
            int slotId = Integer.parseInt(sc.nextLine());

            System.out.print("Enter number of participants: ");
            int participants = Integer.parseInt(sc.nextLine());

            System.out.println("Choose Appointment Type:");
            AppointmentType_y[] types = AppointmentType_y.values();

            for (int i = 0; i < types.length; i++) {
                System.out.println((i + 1) + "- " + types[i]);
            }

            AppointmentType_y selectedType = null;

            while (selectedType == null) {
                try {
                    System.out.print("Enter type number: ");
                    int typeChoice = Integer.parseInt(sc.nextLine());

                    if (typeChoice < 1 || typeChoice > types.length) {
                        System.out.println("Invalid choice, please select a valid number.");
                    } else {
                        selectedType = types[typeChoice - 1];
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Invalid input! Please enter a number.");
                }
            }

            boolean success = slotService.bookAppointment(
                    user.getAccountId(),
                    slotId,
                    participants,
                    selectedType
            );

            if (success) {
                System.out.println("Appointment booked successfully!");
            } else {
                System.out.println("Booking failed!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void updateAppointment() {
        while (true) {
            try {
                System.out.print("Enter appointment ID to update: ");
                int appointmentId = Integer.parseInt(sc.nextLine());

                System.out.print("Enter new number of participants: ");
                int participants = Integer.parseInt(sc.nextLine());

                boolean success = slotService.updateAppointment(user.getAccountId(), appointmentId, participants);
                if (success) {
                    System.out.println("Appointment updated successfully!");
                } else {
                    System.out.println("Update failed! Check appointment ID or participants.");
                }
                break; // نخرج من اللوب بعد المحاولة
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter numbers only.");
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                break;
            }
        }
    }

    public boolean adminCancelAppointment(int appointmentId) throws SQLException {
        try (Connection conn = database_connection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1️⃣ إيجاد الموعد
                Appointment appointment = appointmentRepo.findById(appointmentId, conn);
                if (appointment == null) {
                    conn.rollback();
                    System.out.println("Appointment not found!");
                    return false;
                }

                int slotId = appointment.getSlotId();
                int participants = appointment.getParticipants();
                int userId = appointment.getUserId();

                // 2️⃣ حذف الموعد
                appointmentRepo.delete(appointmentId, conn);

                // 3️⃣ تحديث عدد الحجوزات في الـ Slot
                slotRepo.decreaseBookedCount(slotId, participants, conn);

                conn.commit();

                // 4️⃣ إرسال إشعار للمستخدم
                System.out.println("User " + userId + ": Your appointment was cancelled by admin.");

                // 5️⃣ التحقق إذا مازال نفس الـ Slot متاح لإعادة الحجز
                AppointmentSlot_y slot = slotRepo.findById(slotId);
                if (slot != null && (slot.getMaxCapacity() - slot.getBookedCount() >= participants)) {
                    System.out.println("Slot is still available. User can rebook the same slot if desired.");
                    // هنا ممكن تستدعي دالة الحجز تلقائياً أو تنتظر اختيار المستخدم
                }

                return true;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    private static void addSlot() {
        while (true) {
            try {
                System.out.print("Enter date (YYYY-MM-DD): ");
                LocalDate date = LocalDate.parse(sc.nextLine());

                System.out.print("Enter start time (HH:MM): ");
                LocalTime start = LocalTime.parse(sc.nextLine());

                System.out.print("Enter end time (HH:MM): ");
                LocalTime end = LocalTime.parse(sc.nextLine());

                System.out.print("Enter max capacity: ");
                int capacity = Integer.parseInt(sc.nextLine());

                slotService.addSlot(session.getAccount(), date, start, end, capacity);
                System.out.println("Slot added successfully!");
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number input! Try again.");
            } catch (Exception e) {
                System.out.println("Error adding slot: " + e.getMessage());
            }
        }
    }

    private static void adminCancelAppointment() {
        while (true) {
            System.out.print("Enter appointment ID to cancel by admin: ");
            String input = sc.nextLine();
            int appointmentId;

            try {
                appointmentId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID! Please enter a valid number.");
                continue; // يرجع يطلب مرة ثانية
            }

            try {
                boolean success = slotService.adminCancelAppointment(appointmentId);
                if (success) {
                    System.out.println("Appointment cancelled successfully!");
                } else {
                    System.out.println("Appointment ID not found or could not be cancelled.");
                }
                break; // نخرج من اللوب بعد محاولة ناجحة
            } catch (SQLException e) {
                System.out.println("Error cancelling appointment: " + e.getMessage());
                break;
            }
        }
    }}