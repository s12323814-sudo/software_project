package admain;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class EmailNotificationAdapterTest {

    @Test
    void testSendReminder_callsEmailService() {

        EmailService_y emailService = mock(EmailService_y.class);

        EmailNotificationAdapter adapter =
                new EmailNotificationAdapter(emailService);

        adapter.sendReminder("test@mail.com", "Hello");

        verify(emailService).sendEmail(
                "test@mail.com",
                "Appointment Reminder",
                "Hello"
        );
    }

    @Test
    void testSendNotification_doesNothing() {

        EmailService_y emailService = mock(EmailService_y.class);
        EmailNotificationAdapter adapter =
                new EmailNotificationAdapter(emailService);

        // لازم ما يرمي error
        adapter.sendNotification(1, "msg");

        verifyNoInteractions(emailService);
    }

    @Test
    void testClear_doesNothing() {

        EmailService_y emailService = mock(EmailService_y.class);
        EmailNotificationAdapter adapter =
                new EmailNotificationAdapter(emailService);

        adapter.clear();

        verifyNoInteractions(emailService);
    }

    @Test
    void testGetSentMessages_returnsEmptyList() {

        EmailService_y emailService = mock(EmailService_y.class);
        EmailNotificationAdapter adapter =
                new EmailNotificationAdapter(emailService);

        assert adapter.getSentMessages().isEmpty();
    }
}