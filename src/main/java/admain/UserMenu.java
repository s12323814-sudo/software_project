package admain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class UserMenu {
	 private static SlotService slotService = new SlotService();
    private static Scanner sc = new Scanner(System.in);
   

    public static void showMenu() {

        while (true) {
            System.out.println("\n=== User Menu ===");
            System.out.println("1- View Available Appointment Slots");
          
            System.out.println("2- Exit");

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

 
}