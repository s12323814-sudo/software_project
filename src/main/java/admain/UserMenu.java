package admain;

import java.util.List;
import java.util.Scanner;

public class UserMenu {

    private static Scanner sc = new Scanner(System.in);
    private static SlotService slotService = new SlotService();

    public static void showMenu() {

        while (true) {
            System.out.println("\n=== User Menu ===");
            System.out.println("1- View Available Appointment Slots");
            System.out.println("2- Exit");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    viewAvailableSlots();
                    break;

                case 2:
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
}