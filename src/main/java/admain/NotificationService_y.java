package admain;

import java.util.List;

public interface NotificationServicey {
	void sendReminder(String email, String message);
	void sendNotification(int userId, String message);
	void clear();
	List<String> getSentMessages();
}
