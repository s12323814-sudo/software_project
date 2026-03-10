package admain;

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
            System.out.println("2- Book Appointment");
            System.out.println("3- Logout");

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
                    bookAppointment();
                    break;

                case 3:
                    session_y.logoutUser();
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
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