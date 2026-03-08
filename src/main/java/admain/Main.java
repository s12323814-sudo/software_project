package admain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);
    private static SlotService slotService = new SlotService();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1- Login as User");
            System.out.println("2- Login as Admin");
            System.out.println("3- Exit");

            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1: UserMenu.showMenu(); break;
                case 2: adminMenu(); break;
                case 3: System.exit(0);
                default: System.out.println("Invalid choice!");
            }
        }
    }

    public static void adminMenu() {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1- Login");
            System.out.println("2- Create Account");
            System.out.println("3- Back");

            int choice = Integer.parseInt(sc.nextLine());
            switch (choice) {
                case 1:
                    System.out.print("Username: "); String username = sc.nextLine();
                    System.out.print("Password: "); String password = sc.nextLine();
                    Admin admin = login_foradmain.login(username, password);
                    if (admin != null) { session.currentAdmin = admin; adminSession(); }
                    else System.out.println("Login Failed!");
                    break;
                case 2:
                    System.out.print("New Username: "); String newUser = sc.nextLine();
                    System.out.print("New Password: "); String newPass = sc.nextLine();
                    System.out.print("Email: "); String email = sc.nextLine();
                    if (login_foradmain.register(newUser, newPass, email)) System.out.println("Admin account created!");
                    else System.out.println("Account creation failed!");
                    break;
                case 3: return;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    public static void adminSession() {
        while (session.currentAdmin != null) {
            System.out.println("\nWelcome Admin: " + session.currentAdmin.getUsername());
            System.out.println("1- View Slots");
            System.out.println("2- Add Slot");
            System.out.println("3- Logout");

            int choice = Integer.parseInt(sc.nextLine());
            switch (choice) {
                case 1: viewAvailableSlots(); break;
                case 2: addSlot(); break;
                case 3: session.logout(); break;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static void addSlot() {
        try {
            System.out.print("Enter Date (yyyy-MM-dd): "); LocalDate date = LocalDate.parse(sc.nextLine());
            System.out.print("Enter Start Time (HH:mm): "); LocalTime start = LocalTime.parse(sc.nextLine());
            System.out.print("Enter End Time (HH:mm): "); LocalTime end = LocalTime.parse(sc.nextLine());

            long duration = Duration.between(start, end).toMinutes();
            if (duration < 30 || duration > 120) { System.out.println("Invalid duration! Must be 30-120 min."); return; }

            System.out.print("Enter Capacity (Max 5): "); int capacity = Math.min(Integer.parseInt(sc.nextLine()), 5);
            int adminId = session.currentAdmin.getAdminId();

            slotService.addSlot(date, start, end, capacity, adminId);
            System.out.println("Slot added successfully!");
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    private static void viewAvailableSlots() {
        List<AppointmentSlot> slots = slotService.getAvailableSlots();
        if (slots.isEmpty()) { System.out.println("No available slots."); return; }

        System.out.printf("%-5s %-12s %-10s %-10s %-10s %-10s%n","ID","Date","Start","End","Capacity","Remaining");
        for (AppointmentSlot slot : slots) {
            int remaining = slot.getMaxCapacity() - slot.getBookedCount();
            System.out.printf("%-5d %-12s %-10s %-10s %-10d %-10d%n",
                    slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(),
                    slot.getMaxCapacity(), remaining);
        }
    }
}