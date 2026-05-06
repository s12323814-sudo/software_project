package admain;


import java.util.ArrayList;
import java.util.List;

public class MockNotificationServicey implements NotificationServicey {

    private List<String> sentMessages = new ArrayList<>();

    @Override
    public void sendReminder(String email, String message) {
        String log = "Mock sent to " + email + ": " + message;
        sentMessages.add(log);
        System.out.println(log);
    }

    public void sendNotification(int userId, String message) {
        String log = "Mock Notification to UserID " + userId + ": " + message;
        sentMessages.add(log);
        System.out.println(log);
    }
    public List<String> getSentMessages() {
        return sentMessages;
    }

    public void clear() {
        sentMessages.clear();
    }
}
