package admain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MockNotificationService_yTest {

    @Test
    void testSendReminder_addsMessage() {

        MockNotificationService_y service = new MockNotificationService_y();

        service.sendReminder("test@mail.com", "Hello");

        List<String> messages = service.getSentMessages();

        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("test@mail.com"));
        assertTrue(messages.get(0).contains("Hello"));
    }

    @Test
    void testSendNotification_addsMessage() {

        MockNotificationService_y service = new MockNotificationService_y();

        service.sendNotification(1, "Hi User");

        List<String> messages = service.getSentMessages();

        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("UserID 1"));
        assertTrue(messages.get(0).contains("Hi User"));
    }

    @Test
    void testClear_removesAllMessages() {

        MockNotificationService_y service = new MockNotificationService_y();

        service.sendReminder("a@a.com", "msg1");
        service.sendNotification(2, "msg2");

        assertEquals(2, service.getSentMessages().size());

        service.clear();

        assertTrue(service.getSentMessages().isEmpty());
    }

    @Test
    void testGetSentMessages_returnsSameListReference() {

        MockNotificationService_y service = new MockNotificationService_y();

        service.sendNotification(5, "test");

        List<String> list = service.getSentMessages();

        assertNotNull(list);
        assertEquals(1, list.size());
    }
}