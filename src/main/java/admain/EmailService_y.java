package admain;

public interface EmailServicey {
    void sendOTP(String toEmail, String otp);
    void sendEmail(String toEmail, String subject, String message);
    
}
