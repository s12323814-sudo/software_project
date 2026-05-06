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
	
	private static final Account_y Account_y = null;

	static Scanner sc = new Scanner(System.in);

	private static AppointmentRepository_y appointmentRepo = new AppointmentRepository_y();
	private static SlotRepository_y slotRepo = new SlotRepository_y();

	private static EmailService_y emailService = new EmailSender_y();

	private static NotificationService_y notificationService =
	        new EmailNotificationAdapter(emailService);

	private static SlotService_y slotService =
	        new SlotService_y(appointmentRepo, slotRepo, notificationService, emailService);

	private static authService_y authService = new authService_y();

	private static session_y session;
	private static Account_y user;

	private static ReminderManager_y reminderManager =
	        new ReminderManager_y(appointmentRepo, notificationService);
    public static void main(String[] args) {
    	 ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    	 scheduler.scheduleAtFixedRate(() -> {
    		    reminderManager.checkReminders();
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

        user = session.getAccount();
        
        // تعيين الـ user في الجلسة العامة
        if (session.isAdmin()) {
            session_y.currentAdmin = user;
        } else {
            session_y.currentUser = user;
            List<String> messages = notificationService.getSentMessages();
            if (!messages.isEmpty()) {
                System.out.println("\n--- Notifications ---");
                messages.forEach(System.out::println);
                notificationService.clear(); 
            }
        }

        System.out.println("Login successful! Role: " + user.getRole());

        if (session.isAdmin())
			try {
				adminSession(Account_y);
			} catch (SQLException e) {
			 logger.error("Error fetching account from database", e);
			}
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
        String roleInput = sc.nextLine().trim();

        try {
            Role_y role = Role_y.fromString(roleInput); 

            Account_y acc = authService.register(username, password, email, role);

            if (acc != null)
                System.out.println("Account created successfully!");
            else
                System.out.println("Registration failed!");

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void forgotPasswordMenu() {

        System.out.print("Enter your email: ");
        String email = sc.nextLine();

        if (!authService.emailExists(email)) {
            System.out.println("Email not found!");
            return;
        }

        String otp = OTPGenerator_y.generateOTP();

        emailService.sendOTP(email, otp);

        System.out.print("Enter the OTP sent to your email: ");
        String enteredOtp = sc.nextLine();

        if (!otp.equals(enteredOtp)) {
            System.out.println("Invalid OTP!");
            return;
        }

        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine();

        if (authService.updatePassword(email, newPassword))
            System.out.println("Password updated successfully!");
        else
            System.out.println("Failed to update password!");
    }
    
    // -------------------- User Menu --------------------
    private static void userSession() {
        SlotRepository_y slotRepo = new SlotRepository_y();
        BookingSmartService smart = new BookingSmartService(slotRepo);

        while (session != null && session.isUser()) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1- View Available Slots");
            System.out.println("2- View My Appointments");
            System.out.println("3- Show Nearest Available Slot");
            System.out.println("4- Show Best Slot (Less Busy)");
            System.out.println("5- Sort Slots By Time");
            System.out.println("6- Sort Slots By Availability");
            System.out.println("7- Book Appointment");
            System.out.println("8- Update Appointment");
            System.out.println("9- Cancel MY Appointment");
            System.out.println("10- Logout");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (Exception e) { choice = -1; }

            try {
                switch (choice) {
                    case 1:
                        viewAvailableSlots(); 
                        break;
                    case 2:
                        viewUserAppointments(); 
                        break;

                    // ----- Smart Service Features -----
                    case 3: // Nearest Slot
                        AppointmentSlot_y nearest = smart.getNearestAvailableSlot();
                        System.out.println("\n--- Nearest Available Slot ---");
                        if (nearest != null) {
                            System.out.println("Date: " + nearest.getDate());
                            System.out.println("Start: " + nearest.getStartTime());
                            System.out.println("End: " + nearest.getEndTime());
                            System.out.println("Available: " + (nearest.getMaxCapacity() - nearest.getBookedCount()));
                        } else {
                            System.out.println("No available slots.");
                        }
                        break;

                    case 4: // Best Slot (Less Busy)
                        AppointmentSlot_y best = smart.getBestSlot();
                        System.out.println("\n--- Best Slot (Less Busy) ---");
                        if (best != null) {
                            System.out.println("Date: " + best.getDate());
                            System.out.println("Start: " + best.getStartTime());
                            System.out.println("End: " + best.getEndTime());
                            System.out.println("Available: " + (best.getMaxCapacity() - best.getBookedCount()));
                        } else {
                            System.out.println("No available slots.");
                        }
                        break;

                    case 5: // Sort By Time
                        List<AppointmentSlot_y> sortedByTime = smart.sortByTime();
                        System.out.println("\n--- Slots Sorted By Time ---");
                        System.out.printf("%-5s %-12s %-8s %-8s %-10s\n", "ID", "Date", "Start", "End", "Available");
                        for (AppointmentSlot_y s : sortedByTime) {
                            int available = s.getMaxCapacity() - s.getBookedCount();
                            System.out.printf("%-5d %-12s %-8s %-8s %-10d\n",
                                    s.getId(), s.getDate(), s.getStartTime(), s.getEndTime(), available);
                        }
                        break;

                    case 6: // Sort By Availability
                        List<AppointmentSlot_y> sortedByAvail = smart.sortByAvailability();
                        System.out.println("\n--- Slots Sorted By Availability ---");
                        System.out.printf("%-5s %-12s %-8s %-8s %-10s\n", "ID", "Date", "Start", "End", "Available");
                        for (AppointmentSlot_y s : sortedByAvail) {
                            int available = s.getMaxCapacity() - s.getBookedCount();
                            System.out.printf("%-5d %-12s %-8s %-8s %-10d\n",
                                    s.getId(), s.getDate(), s.getStartTime(), s.getEndTime(), available);
                        }
                        break;

                    // ----- Original Options -----
                    case 7:
                        bookAppointment(); 
                        break;
                    case 8:
                        updateAppointment(); 
                        break;
                    case 9:
                        cancelMyAppointment(); 
                        break;
                    case 10:
                        session_y.logoutUser(); 
                        session = null; 
                        break;
                    default:
                        System.out.println("Invalid choice"); 
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    // -------------------- Admin Menu --------------------
    private static void adminSession(Account_y currentUser) throws SQLException {
        SlotRepository_y slotRepo = new SlotRepository_y();
        BookingSmartService smart = new BookingSmartService(slotRepo);

        while (session != null && session.isAdmin()) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1- View Slots");
            System.out.println("2- View Most Available Slots");
            System.out.println("3- View Nearest Slot");
            System.out.println("4- Add Slot");
            System.out.println("5- Cancel Slot");
            System.out.println("6- VIEW ALL Appointment");
            System.out.println("7- Cancel Appointment");
            System.out.println("8- Logout");

            int choice;

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid input! Enter a number.");
                continue; 
            }

            try {
                switch(choice) {
                    case 1: 
                        viewAvailableSlots(); 
                        break;

                    // ----- Smart Features -----
                    case 2: // Most Available Slots
                        List<AppointmentSlot_y> mostAvailable = smart.sortByAvailability();
                        System.out.println("\n--- Slots Sorted By Availability ---");
                        System.out.printf("%-5s %-12s %-8s %-8s %-10s\n", "ID", "Date", "Start", "End", "Available");
                        for (AppointmentSlot_y s : mostAvailable) {
                            int available = s.getMaxCapacity() - s.getBookedCount();
                            System.out.printf("%-5d %-12s %-8s %-8s %-10d\n",
                                    s.getId(), s.getDate(), s.getStartTime(), s.getEndTime(), available);
                        }
                        break;

                    case 3: // Nearest Slot
                        AppointmentSlot_y nearest = smart.getNearestAvailableSlot();
                        System.out.println("\n--- Nearest Available Slot ---");
                        if (nearest != null) {
                            System.out.println("Date: " + nearest.getDate());
                            System.out.println("Start: " + nearest.getStartTime());
                            System.out.println("End: " + nearest.getEndTime());
                            System.out.println("Available: " + (nearest.getMaxCapacity() - nearest.getBookedCount()));
                        } else {
                            System.out.println("No available slots.");
                        }
                        break;

                    case 4: 
                        addSlot(); 
                        break;

                    case 5:
                    	
                        System.out.print("Enter Slot ID to cancel: ");
                        int slotId = Integer.parseInt(sc.nextLine());
                        if (!slotService.adminCancelSlot(slotId)) {
                            System.out.println("❌ Slot ID not found or could not be deleted.");
                        }
                        break;
                    case 6:
                        viewAllAppointments();
                        break;
                    case 7:
                    	System.out.print("Enter Appointment ID to cancel: ");
                    	int appointmentId = Integer.parseInt(sc.nextLine());
					int adminId = currentUser.getAccountId(); 

                    	if (!slotService.adminCancelAppointment(adminId, appointmentId)) {
                    	    System.out.println("❌ Appointment ID not found or not allowed to cancel.");
                    	} else {
                    	    System.out.println("✅ Appointment cancelled successfully.");
                    	}
                        break;               

                    case 8: 
                        session_y.logoutAdmin();    
                        return;
                    case 9:
                        viewAllAppointments();
                        break;
                    default: 
                        System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    private static void cancelMyAppointment() {
        try {
            System.out.print("Enter your Appointment ID to cancel: ");
            int appointmentId = Integer.parseInt(sc.nextLine());

            boolean success = slotService.cancelAppointment(user.getAccountId(), appointmentId);

            if (success) {
                System.out.println("✅ Your appointment cancelled successfully!");
            } else {
                System.out.println("❌ Cancel failed! Check ID.");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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
logger.error("Error fetching account from database", e);
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
          logger.error("Error fetching account from database", e);
        }
    }
   
    private static void bookAppointment() {
        try {
            System.out.print("Enter slot ID: ");
            int slotId = Integer.parseInt(sc.nextLine());
            
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
            switch (selectedType) {

            case URGENT:
                System.out.println("⚠️ URGENT appointment:");
                System.out.println("- Only 1 participant allowed.");
                System.out.println("- Priority will be given.");
                break;

            case INDIVIDUAL:
                System.out.println("ℹ️ INDIVIDUAL appointment:");
                System.out.println("- Only 1 participant is allowed.");
                break;

            case GROUP:
                System.out.println("ℹ️ GROUP appointment:");
                System.out.println("- Minimum 2 participants required.");
                System.out.println("- Make sure slot has enough capacity.");
                break;

            case VIRTUAL:
                System.out.println("🌐 VIRTUAL appointment:");
                System.out.println("- No physical capacity limit.");
                System.out.println("- You can add many participants.");
                break;

            case IN_PERSON:
                System.out.println("🏥 IN-PERSON appointment:");
                System.out.println("- Limited by slot capacity.");
                System.out.println("- Check available seats before booking.");
                break;

            case FOLLOW_UP:
                System.out.println("🔁 FOLLOW-UP appointment:");
                System.out.println("- Usually short and for existing cases.");
                System.out.println("- Recommended 1 participant.");
                break;

            case ASSESSMENT:
                System.out.println("📋 ASSESSMENT appointment:");
                System.out.println("- Initial evaluation session.");
                System.out.println("- Typically 1 participant.");
                break;

            case GENERAL:
                System.out.println("ℹ️ GENERAL appointment:");
                System.out.println("- Flexible type.");
                System.out.println("- Follow slot capacity rules.");
                break;

            default:
                System.out.println("Unknown type.");
        }
            System.out.print("Enter number of participants: ");
            int participants = Integer.parseInt(sc.nextLine());

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
         logger.error("Error fetching account from database", e);
        }
    }
    private static void viewAllAppointments() {
        try {
            List<Appointment> list = slotService.getAllAppointments(user.getAccountId());

            if (list.isEmpty()) {
                System.out.println("No appointments found.");
                return;
            }

            System.out.printf("%-5s %-10s %-8s %-13s %-10s %-10s\n",
                    "ID", "User", "Slot", "Participants", "Status", "Type");

            for (Appointment a : list) {
                System.out.printf("%-5d %-10s %-8d %-13d %-10s %-10s\n",
                        a.getAppointmentId(),
                        a.getUsername(),
                        a.getSlotId(),
                        a.getParticipants(),
                        a.getStatus(),
                        a.getType());
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
  private static void updateAppointment() {
    boolean validInput = false;
    while (!validInput) {
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
            validInput = true; // exit after successful attempt
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter numbers only.");
            // loop continues naturally, prompting again
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            validInput = true; // exit on database error
        }
    }
}

   

    private static void addSlot() {
    	while (true) {
    	    try {
    	        System.out.print("Enter date (YYYY-MM-DD): ");
    	        LocalDate date = LocalDate.parse(sc.nextLine());

    	        if (date.isBefore(LocalDate.now())) {
    	            System.out.println("Error: Date cannot be in the past!");
    	            continue;
    	        }

    	        System.out.print("Enter start time (HH:MM): ");
    	        LocalTime start = LocalTime.parse(sc.nextLine());

    	        System.out.print("Enter end time (HH:MM): ");
    	        LocalTime end = LocalTime.parse(sc.nextLine());

    	        if (date.isEqual(LocalDate.now())) {
    	            if (start.isBefore(LocalTime.now())) {
    	                System.out.println("Error: Start time cannot be in the past!");
    	                continue;
    	            }
    	        }

    	        if (end.isBefore(start) || end.equals(start)) {
    	            System.out.println("Error: End time must be after start time!");
    	            continue;
    	        }

    	        System.out.print("Enter max capacity: ");
    	        String capInput = sc.nextLine();

    	        int capacity;
    	        try {
    	            capacity = Integer.parseInt(capInput);
    	            if (capacity <= 0) {
    	                System.out.println("Error: Capacity must be greater than 0!");
    	                continue;
    	            }
    	        } catch (NumberFormatException e) {
    	            System.out.println("Error: Capacity must be a valid number!");
    	            continue;
    	        }

    	        slotService.addSlot(date, start, end, capacity, user.getAccountId());
    	        System.out.println("Slot added successfully!");
    	        break;

    	    } catch (java.time.format.DateTimeParseException e) {
    	        System.out.println("Error: Invalid date/time format!");
    	    } catch (Exception e) {
    	        System.out.println("Error adding slot: " + e.getMessage());
    	    }
    	}}
}
