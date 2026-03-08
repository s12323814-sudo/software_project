package admain;

import java.util.ArrayList;
import java.util.List;

public class MockNotificationService implements NotificationService {

    private List<String> sentMessages = new ArrayList<>();

    @Override
    public void sendNotification(String email, String message) {
        String log = "Mock sent to " + email + ": " + message;
        sentMessages.add(log);
        System.out.println(log); // للعرض أثناء الاختبار
    }

    public List<String> getSentMessages() {
        return sentMessages;
    }

    public void clear() {
        sentMessages.clear();
    }
}