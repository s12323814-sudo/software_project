package admain;

import java.util.List;

public interface NotificationService_y {
	void sendReminder(String email, String message);
	void sendNotification(int userId, String message);
	void clear();
	List<String> getSentMessages();
}