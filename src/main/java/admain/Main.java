package admain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    private static SlotService slotService = new SlotService();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {

        while (true) {
            System.out.println("=== Main Menu ===");
            System.out.println("1- Login as User");
            System.out.println("2- Login as Admin");
            System.out.println("3- Exit");

            int choice = sc.nextInt();
            sc.nextLine(); // لتنظيف الـ buffer

            switch (choice) {
                case 1:
                    UserMenu.showMenu();
                    break;
                case 2:
                    adminMenu();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice!");
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
            sc.nextLine(); // لتنظيف buffer

            switch (choice) {

                case 1:
                    System.out.print("Username: ");
                    String username = sc.nextLine();

                    System.out.print("Password: ");
                    String password = sc.nextLine();

                    Admin admin = login_foradmain.login(username, password);

                    if (admin != null) {
                        session.currentAdmin = admin;
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

                    if (login_foradmain.register(newUser, newPass, email)) {
                        System.out.println("Account created successfully!");
                    } else {
                        System.out.println("Account creation failed!");
                    }
                    break;

                case 3:
                    forgotPassword();
                    break;

                case 4:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public static void forgotPassword() {

        System.out.print("Enter your email: ");
        String email = sc.nextLine();

        if (!login_foradmain.emailExists(email)) {
            System.out.println("Email not found!");
            return;
        }

        String otp = OTPGenerator.generateOTP();
        EmailSender.sendOTP(email, otp);

        System.out.print("Enter OTP sent to your email: ");
        String userOtp = sc.nextLine();

        if (otp.equals(userOtp)) {
            System.out.print("Enter new password: ");
            String newPassword = sc.nextLine();

            if (login_foradmain.updatePassword(email, newPassword)) {
                System.out.println("Password updated successfully!");
            }

        } else {
            System.out.println("Wrong OTP.");
        }
    }

    public static void adminSession() {

        while (session.currentAdmin != null) {

            System.out.println("\nWelcome Admin: " + session.currentAdmin.getUsername());
            System.out.println("1- View Slots");
            System.out.println("2- Book Appointment");
            System.out.println("3- Logout");

            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1:
                    viewAvailableSlots();
                    break;
                case 2:
                    bookAppointment();
                    break;
                case 3:
                    session.logout();
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void bookAppointment() {

        try {
            System.out.print("Enter Slot ID: ");
            int slotId = Integer.parseInt(sc.nextLine());

            System.out.print("Enter Start Time (yyyy-MM-dd HH:mm): ");
            LocalDateTime start = LocalDateTime.parse(sc.nextLine(), formatter);

            System.out.print("Enter End Time (yyyy-MM-dd HH:mm): ");
            LocalDateTime end = LocalDateTime.parse(sc.nextLine(), formatter);

            System.out.print("Enter Number of Participants: ");
            int participants = Integer.parseInt(sc.nextLine());

            slotService.bookSlot(slotId, start, end, participants);

            System.out.println("Appointment booked successfully!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid number input.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewAvailableSlots() {
        List<AppointmentSlot> slots = slotService.getAvailableSlots();

        if (slots.isEmpty()) {
            System.out.println("No available slots.");
            return;
        }

        System.out.println("\nAvailable Slots:");
        System.out.printf("%-5s %-12s %-8s %-10s %-10s %-10s%n",
                "ID", "Date", "Time", "Capacity", "Booked", "Remaining");

        for (AppointmentSlot slot : slots) {
            int remaining = slot.getMaxCapacity() - slot.getBookedCount();
            if (remaining > 0) {
                System.out.printf("%-5d %-12s %-8s %-10d %-10d %-10d%n",
                        slot.getId(),
                        slot.getDate(),
                        slot.getTime(),
                        slot.getMaxCapacity(),
                        slot.getBookedCount(),
                        remaining
                );
            }
        }
    }
}