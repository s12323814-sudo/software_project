package admain;

import static org.mockito.Mockito.*;
import javax.mail.Message;
import org.junit.jupiter.api.Test;

public class EmailSenderTest {

    @Test
    void testSendOTP() throws Exception {

        EmailSender_y sender = spy(new EmailSender_y());

        doNothing().when(sender).send(any(Message.class));

        sender.sendOTP("test@email.com", "123456");

        verify(sender, times(1)).send(any(Message.class));
    }
}