package admain;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EmailSender_yTest {


// ─── Subclass تـ override send() فقط ─────────────────────
static class TestableEmailSender extends EmailSender_y {
    boolean sendCalled = false;
    Message lastMessage = null;
    boolean throwError = false;

    @Override
    protected void send(Message message) throws MessagingException {
        if (throwError) throw new MessagingException("Simulated failure");
        sendCalled = true;
        lastMessage = message;
    }
}

private TestableEmailSender sender;

@BeforeEach
void setup() throws Exception {
    sender = new TestableEmailSender();
    // نحقن fromEmail وpassword بقيم وهمية عبر Reflection
    // عشان ما يطلع NullPointerException من dotenv
    setField(sender, "fromEmail", "fake@example.com");
    setField(sender, "password",  "fakepassword");
}

// ─── Helper: يحقن قيمة في field خاص عبر Reflection ──────
private void setField(Object target, String fieldName, String value)
        throws Exception {
    Field f = EmailSender_y.class.getDeclaredField(fieldName);
    f.setAccessible(true);
    f.set(target, value);
}

// ─── 1. sendEmail ينجح ويستدعي send() ───────────────────
@Test
void testSendEmail_CallsSend() {
    sender.sendEmail("to@example.com", "Hi", "Body");
    assertTrue(sender.sendCalled, "send() must be called");
}

// ─── 2. subject صح ───────────────────────────────────────
@Test
void testSendEmail_CorrectSubject() throws Exception {
    sender.sendEmail("to@example.com", "My Subject", "Body");
    assertNotNull(sender.lastMessage);
    assertEquals("My Subject", sender.lastMessage.getSubject());
}

// ─── 3. body صح ──────────────────────────────────────────
@Test
void testSendEmail_CorrectBody() throws Exception {
    sender.sendEmail("to@example.com", "Subj", "Hello World");
    assertNotNull(sender.lastMessage);
    assertEquals("Hello World", sender.lastMessage.getContent().toString());
}

// ─── 4. send() يرمي exception — ما يطلع للخارج ──────────
@Test
void testSendEmail_WhenSendFails_NoExceptionThrown() {
    sender.throwError = true;
    assertDoesNotThrow(() ->
        sender.sendEmail("to@example.com", "Subj", "Body")
    );
}

// ─── 5. sendOTP يستدعي send() ────────────────────────────
@Test
void testSendOTP_CallsSend() {
    sender.sendOTP("to@example.com", "123456");
    assertTrue(sender.sendCalled);
}

// ─── 6. sendOTP body يحتوي الـ OTP ───────────────────────
@Test
void testSendOTP_BodyContainsOTP() throws Exception {
    sender.sendOTP("to@example.com", "654321");
    assertNotNull(sender.lastMessage);
    assertTrue(sender.lastMessage.getContent().toString().contains("654321"));
}

// ─── 7. sendOTP subject صح ───────────────────────────────
@Test
void testSendOTP_CorrectSubject() throws Exception {
    sender.sendOTP("to@example.com", "000000");
    assertNotNull(sender.lastMessage);
    assertEquals("Password Reset Code", sender.lastMessage.getSubject());
}

// ─── 8. استدعاءات متعددة ─────────────────────────────────
@Test
void testSendEmail_MultipleCalls() {
    sender.sendEmail("a@example.com", "S1", "B1");
    assertTrue(sender.sendCalled);
    sender.sendCalled = false;
    sender.sendEmail("b@example.com", "S2", "B2");
    assertTrue(sender.sendCalled);
}


}