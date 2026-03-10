package admain;

	import java.time.LocalDateTime;
	import java.time.format.DateTimeFormatter;
	import java.util.Scanner;
	import java.util.concurrent.Executors;
	import java.util.concurrent.ScheduledExecutorService;
	import java.util.concurrent.TimeUnit;

	public class Main {

	    static Scanner sc = new Scanner(System.in);
	    private static SlotService_y slotService = new SlotService_y();
	    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	 
	    private static NotificationService_y notificationService = new MockNotificationService_y(); 
	    private static ReminderManager_y reminderManager = new ReminderManager_y(slotService, notificationService);
	    public static void main(String[] args) {
	    	
	    	   ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	    	    scheduler.scheduleAtFixedRate(() -> {
	    	        if (session_y.currentUser != null) {
	    	            reminderManager.checkReminders();
	    	        }
	    	        if (session_y.currentAdmin != null) {
	    	            reminderManager.checkReminders();
	    	        }
	    	    }, 0, 1, TimeUnit.MINUTES);

	        while (true) {
	            System.out.println("=== Main Menu ===");
	            System.out.println("1- Login as User");
	            System.out.println("2- Login as Admin");
	            System.out.println("3- Exit");

	            int choice = sc.nextInt();

	            switch (choice) {
	                case 1:
	                    UserMenu_y.showMenu();
	                    break;

	                case 2:
	                    adminMenu();
	                    break;

	                case 3:
	                    System.out.println("Goodbye!");
	                    System.exit(0);
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

	            int choice = sc.nextInt();
	            sc.nextLine();

	            switch (choice) {

	                case 1:

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

	                    break;

	                case 2:


	                    System.out.print("New Username: ");
	                    String newUser = sc.nextLine();

	                    System.out.print("New Password: ");
	                    String newPass = sc.nextLine();

	                    System.out.print("Email: ");
	                    String email = sc.nextLine();

	                    Admin_y adminR = login_foradmin_y.register(newUser, newPass, email);
	                    if(adminR != null){
	                        session_y.currentAdmin = adminR;
	                        System.out.println("Account created and logged in!");
	                        adminSession();
	                    }
	                     else {
	                        System.out.println("Account creation failed!");
	                    }

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
	            System.out.println("1- Logout");
	            System.out.println("2- book");

	            int choice = Integer.parseInt(sc.nextLine());

	            if (choice == 1) {
	                session_y.logout();
	            } else if (choice == 2) {
	                bookAppointment();
	            }
	        }
	    }

	    private static void bookAppointment() {

	        try {

	            System.out.print("Enter Slot ID: ");
	            int slotId = Integer.parseInt(sc.nextLine());

	            System.out.print("Enter Number of Participants: ");
	            int participants = Integer.parseInt(sc.nextLine());

	            slotService.bookSlot(slotId, participants);

	            System.out.println("Appointment booked successfully!");

	        } catch (NumberFormatException e) {
	            System.out.println("Invalid number input.");
	        } catch (Exception e) {
	            System.out.println("Error: " + e.getMessage());
	        }
	    
	}

}
