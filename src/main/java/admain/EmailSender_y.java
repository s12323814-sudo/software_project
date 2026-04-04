package admain;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender_y implements EmailService_y {

    @Override
    public void sendOTP(String toEmail, String otp) {

        final String fromEmail = "yasmeenalqaduomi@gmail.com";
        final String password = "nlng knkr juiv znqb";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail));

            message.setSubject("Password Reset Code");
            message.setText("Your OTP code is: " + otp);

            send(message); // 👈 بدل Transport.send مباشرة

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // ميثود قابلة للـ override في التست
    protected void send(Message message) throws MessagingException {
        Transport.send(message);
    }
}