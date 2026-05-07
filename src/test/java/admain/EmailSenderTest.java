package admain;

import static org.mockito.Mockito.*;


import org.junit.jupiter.api.Test;

class EmailSenderTest {

	@Test
	void testSendOTP_callsSendEmailOnce() {

	    EmailSender_y sender = spy(new EmailSender_y());

	    doNothing().when(sender)
	            .sendEmail(anyString(), anyString(), anyString());

	    sender.sendOTP("test@email.com", "123456");

	    verify(sender, times(1))
	            .sendEmail(eq("test@email.com"), anyString(), anyString());
	}
	}