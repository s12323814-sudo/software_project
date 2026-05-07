package admain;

public interface EmailService_y {
    void sendOTP(String toEmail, String otp);
    void sendEmail(String toEmail, String subject, String message);
    
}
