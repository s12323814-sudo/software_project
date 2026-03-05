package admain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class UserMenu {

    private static Scanner sc = new Scanner(System.in);
    private static SlotService slotService = new SlotService();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void showMenu() {

        while (true) {
            System.out.println("\n=== User Menu ===");
            System.out.println("1- View Available Appointment Slots");
            System.out.println("2- Book Appointment");
            System.out.println("3- Exit");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
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
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewAvailableSlots() {
        List<AppointmentSlot> slots = slotService.getAvailableSlots();

        if (slots.isEmpty()) {
            System.out.println("No available slots.");
            return;
        }

        System.out.println("\nAvailable Slots:");
        for (AppointmentSlot slot : slots) {
            System.out.println(slot);
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
}