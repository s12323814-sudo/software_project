package admain;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static AppointmentRepository_y appointmentRepository = new AppointmentRepository_y();
    static SlotService_y slotService = new SlotService_y();
    private static NotificationService_y notificationService = new MockNotificationService_y();

    private static ReminderManager_y reminderManager =
            new ReminderManager_y(appointmentRepository, notificationService);
    public static void main(String[] args) {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (session_y.currentUser != null || session_y.currentAdmin != null) {
                reminderManager.checkReminders();
            }
        }, 0, 1, TimeUnit.MINUTES);

        while (true) {
            System.out.println("=== Main Menu ===");
            System.out.println("1- Login as User");
            System.out.println("2- Login as Admin");
            System.out.println("3- Exit");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                continue; }

            switch (choice) {
                case 1 : 
                	UserMenu_y.showMenu();
                	break;
                case 2 :
                	adminMenu();
                	break;
                
                case 3 : 
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default :
                	System.out.println("Invalid choice!");
                	break;
            }
        }
    }

    public static void adminMenu() {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1- Login");
            System.out.println("2- Create Account");
            System.out.println("3- Forgot Password");
            System.out.println("4- Back");
int choice;

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                continue; 
            }

            switch (choice) {
                case 1 : {
                    System.out.print("Username: ");
                    String username = sc.nextLine();
                    System.out.print("Password: ");
                    String password = sc.nextLine();

                    Admin_y admin = login_foradmin_y.login(username, password);
                    if (admin != null) {
                        session_y.currentAdmin = admin;
                        System.out.println("Login Successful!");
                        adminSession();
                    } else {
                        System.out.println("Login Failed!");
                    }
                }
                break;
                case 2 : {
                    System.out.print("New Username: ");
                    String newUser = sc.nextLine();
                    System.out.print("New Password: ");
                    String newPass = sc.nextLine();
                    System.out.print("Email: ");
                    String email = sc.nextLine();

                    Admin_y adminR = login_foradmin_y.register(newUser, newPass, email);
                    if (adminR != null) {
                        session_y.currentAdmin = adminR;
                        System.out.println("Account created and logged in!");
                        adminSession();
                    } else {
                        System.out.println("Account creation failed!");
                    }
                }
                break;
                case 3 :
                	forgotPassword();
                	break;
                case 4 :
                    System.out.println("Exiting..."); 
                    return; 
                default :
                	System.out.println("Invalid choice.");
                	break;
            }
        }
    }

    public static void forgotPassword() {
        System.out.print("Enter your email: ");
        String email = sc.nextLine();

        if (!login_foradmin_y.emailExists(email)) {
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

            if (login_foradmin_y.updatePassword(email, newPassword)) {
                System.out.println("Password updated successfully!");
            }
        } else {
            System.out.println("Wrong OTP.");
        }
    }

    public static void adminSession() {
        while (session_y.currentAdmin != null) {
            System.out.println("\nWelcome Admin: " + session_y.currentAdmin.getUsername());
            System.out.println("1- View Slots");
            System.out.println("2- Add Slot");
            System.out.println("3- cancel Appointment");
            System.out.println("4- Logout");

            int choice ;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                continue; 
            }

            switch (choice) {

            case 1:
                viewAvailableSlots();
                break;

            case 2:
                addSlotInteractive();
                break;

            case 3:
                adminCancelAppointmentInteractive();
                break;

            case 4:
                session_y.logout();
                break;

            default:
                System.out.println("Invalid choice!");
        }}}
   
    	private static void adminCancelAppointmentInteractive() {

    	    try {

    	        System.out.print("Enter Appointment ID to cancel: ");
    	        int appointmentId = Integer.parseInt(sc.nextLine());

    	        boolean result = slotService.adminCancelAppointment(appointmentId);

    	        if (!result) {
    	            System.out.println("Cancellation failed.");
    	        }

    	    } catch (Exception e) {
    	        System.out.println("Error: " + e.getMessage());
    	    }
    	}
    public static void addSlotInteractive() {
        try {
            System.out.print("Enter date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(sc.nextLine());

            System.out.print("Enter start time (HH:MM): ");
            LocalTime start = LocalTime.parse(sc.nextLine());

            System.out.print("Enter end time (HH:MM): ");
            LocalTime end = LocalTime.parse(sc.nextLine());

            System.out.print("Enter max capacity: ");
            int capacity = Integer.parseInt(sc.nextLine());

            int adminId = session_y.currentAdmin.getAdminId();

            slotService.addSlot(date, start, end, capacity, adminId);
            System.out.println("Slot added successfully!");

        } catch (Exception e) {
            System.out.println("Error adding slot: " + e.getMessage());
        }
    }

    private static void viewAvailableSlots() {
        List<AppointmentSlot_y> slots = slotService.getAvailableSlots();
        if (slots.isEmpty()) {
            System.out.println("No available slots.");
            return;
        }

        System.out.printf("%-5s %-12s %-10s %-10s %-10s %-10s%n","ID","Date","Start","End","Capacity","Remaining");
        for (AppointmentSlot_y slot : slots) {
            int remaining = slot.getMaxCapacity() - slot.getBookedCount();
            System.out.printf("%-5d %-12s %-10s %-10s %-10d %-10d%n",
                    slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(),
                    slot.getMaxCapacity(), remaining);
        }
    }
}