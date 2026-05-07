package admain;



public class EmailNotificationAdapter implements NotificationService_y {

    private EmailService_y emailService;

    public EmailNotificationAdapter(EmailService_y emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendReminder(String email, String message) {
        emailService.sendEmail(email, "Appointment Reminder", message);
    }

    @Override
    public void sendNotification(int userId, String message) {}

    @Override
    public void clear() {}

    @Override
    public java.util.List<String> getSentMessages() {
        return java.util.Collections.emptyList();
    }
}
