package admain;

import java.security.SecureRandom;

public class OTPGeneratory {

    private static final SecureRandom rand = new SecureRandom();

    public static String generateOTP() {
        int otp = 100000 + rand.nextInt(900000);
        return String.valueOf(otp);
    }
}
