package admain;

import java.util.Random;

public class OTPGenerator_y {

    public static String generateOTP() {
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000);
        return String.valueOf(otp);
    }
}