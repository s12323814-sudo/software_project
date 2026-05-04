package admain;



import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class ReminderManager_y {

    private AppointmentRepository_y appointmentRepository;
    private NotificationService_y notificationService;

    // لتخزين IDs التي تم إرسال التذكير لها
    private Set<Integer> remindedOneHour = new HashSet<>();
    private Set<Integer> remindedTenMinutes = new HashSet<>();

    public ReminderManager_y(AppointmentRepository_y appointmentRepository,
                             NotificationService_y notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
    }

    public void checkReminders() {
        if (session_y.currentUser == null) return;

        try {
            List<Appointment> upcomingAppointments =
                appointmentRepository.getUserUpcomingAppointments(
                    session_y.currentUser.getAccountId()
                );

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Hebron"));

            for (Appointment appt : upcomingAppointments) {
                ZonedDateTime slotStart = appt.getTimeSlot().getStart();
                long minutesUntilStart = java.time.Duration.between(now, slotStart).toMinutes();

                // قبل ساعة
                if (minutesUntilStart <= 60 && minutesUntilStart > 50 && !remindedOneHour.contains(appt.getAppointmentId())) {
                    sendReminder(appt, "Reminder: Appointment in 1 hour!");
                    remindedOneHour.add(appt.getAppointmentId());
                }

                // قبل 10 دقائق
                else if (minutesUntilStart <= 10 && minutesUntilStart >0&& !remindedTenMinutes.contains(appt.getAppointmentId())) {
                    sendReminder(appt, "Reminder: Appointment in 10 minutes!");
                    remindedTenMinutes.add(appt.getAppointmentId());
                }
            }

        } catch (Exception e) {
        logger.error("Error fetching account from database", e);
        }
    }

    private void sendReminder(Appointment appt, String message) {
        notificationService.sendReminder(session_y.currentUser.getEmail(), message);
        System.out.println("Reminder sent for Appointment ID: " + appt.getTimeSlot().getId() + " | " + message);
    }
}
