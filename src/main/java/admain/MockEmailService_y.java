package admain;



public class MockEmailService_y implements EmailService_y {

    @Override
    public void sendOTP(String toEmail, String otp) {

        System.out.println("----- MOCK EMAIL -----");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: Password Reset Code");
        System.out.println("OTP: " + otp);
        System.out.println("----------------------");
    }
}
