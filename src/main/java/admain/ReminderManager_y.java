package admain;

import java.time.LocalDateTime;
import java.util.List;

public class ReminderManager_y {

    private SlotService_y slotService;
    private NotificationService_y notificationService;

    public ReminderManager_y(SlotService_y slotService, NotificationService_y notificationService) {
        this.slotService = slotService;
        this.notificationService = notificationService;
    }

    public void checkReminders() {

        List<AppointmentSlot_y> upcomingSlots = slotService.getAvailableSlots();
        LocalDateTime now = LocalDateTime.now();

        for (AppointmentSlot_y slot : upcomingSlots) {

            LocalDateTime slotDateTime = slot.getStartDateTime();

            long minutes = java.time.Duration.between(now, slotDateTime).toMinutes();

            if (minutes > 0 && minutes <= 60) {

                if (slot.getBookings().isEmpty()) {
                    continue;
                }

                for (Booking_y booking : slot.getBookings()) {

                    if (session_y.currentUser != null &&
                    		booking.getUserEmail().equals(session_y.currentUser.getEmail())) {

                        String message = "Reminder: Upcoming appointment! Slot ID: "
                                + slot.getId() + " at " + slotDateTime;

                        notificationService.sendReminder(booking.getUserEmail(), message);

                        System.out.println("Reminder sent to " + booking.getUserEmail());
                        System.out.println("Slot ID: " + slot.getId());
                        System.out.println("Date: " + slot.getDate());
                        System.out.println("Start Time: " + slot.getStartTime());
                        System.out.println("End Time: " + slot.getEndTime());
                        System.out.println("---------------------------");
                    }
                }
            }
        }
    }
}