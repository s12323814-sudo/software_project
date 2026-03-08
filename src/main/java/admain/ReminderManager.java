package admain;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReminderManager {

    private SlotService slotService = new SlotService();

    public void checkReminders() {

        List<AppointmentSlot> upcomingSlots = slotService.getAvailableSlots();

        LocalDateTime now = LocalDateTime.now();

        for (AppointmentSlot slot : upcomingSlots) {

            LocalDate date = slot.getDate();
            LocalTime time = slot.getTime();

            LocalDateTime slotDateTime = LocalDateTime.of(date, time);

            long minutes = java.time.Duration.between(now, slotDateTime).toMinutes();

            if (minutes > 0 && minutes <= 60) {

                System.out.println("Reminder:");
                System.out.println("Upcoming appointment!");
                System.out.println("Slot ID: " + slot.getId());
                System.out.println("Date: " + slot.getDate());
                System.out.println("Time: " + slot.getTime());
                System.out.println("---------------------------");

            }
        }
    }
}