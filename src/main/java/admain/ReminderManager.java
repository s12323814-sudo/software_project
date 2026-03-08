package admain;

import java.time.LocalDateTime;
import java.util.List;

public class ReminderManager {

    private SlotService slotService;
    private NotificationService notificationService;

    public ReminderManager(SlotService slotService, NotificationService notificationService) {
        this.slotService = slotService;
        this.notificationService = notificationService;
    }

    public void checkReminders() {

        List<AppointmentSlot> upcomingSlots = slotService.getAvailableSlots();
        LocalDateTime now = LocalDateTime.now();

        for (AppointmentSlot slot : upcomingSlots) {

            // تحويل التاريخ + الوقت إلى LocalDateTime
            LocalDateTime slotDateTime = slot.getDateTime();

            long minutes = java.time.Duration.between(now, slotDateTime).toMinutes();

            // فقط المواعيد القادمة خلال ساعة
            if (minutes > 0 && minutes <= 60) {

                // إرسال تذكير لكل booking في هذا slot
                for (Booking booking : slot.getBookings()) {
                    String message = "Reminder: Upcoming appointment! Slot ID: " 
                                     + slot.getId() + " at " + slotDateTime;
                    notificationService.sendReminder(booking.getUserEmail(), message);

                    // طباعة للتست / العرض
                    System.out.println("Reminder sent to " + booking.getUserEmail());
                    System.out.println("Slot ID: " + slot.getId());
                    System.out.println("Date: " + slot.getDate());
                    System.out.println("Time: " + slot.getTime());
                    System.out.println("---------------------------");
                }
            }
        }
    }
}