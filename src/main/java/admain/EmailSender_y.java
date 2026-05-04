package admain;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import io.github.cdimascio.dotenv.Dotenv;
public class EmailSender_y implements EmailService_y {

  Dotenv dotenv = Dotenv.load();

private final String fromEmail = dotenv.get("EMAIL_USER");
private final String password = dotenv.get("EMAIL_PASS");


    @Override
    public void sendOTP(String toEmail, String otp) {
        sendEmail(toEmail, "Password Reset Code", "Your OTP code is: " + otp);
    }


    @Override
    public void sendEmail(String toEmail, String subject, String messageText) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(messageText);

            send(message);

            System.out.println("Email sent to: " + toEmail);

        } catch (MessagingException e) {
 logger.error("Error fetching account from database", e);
        }
    }

    protected void send(Message message) throws MessagingException {
        Transport.send(message);
    }
}
