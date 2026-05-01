package admain;

import java.util.Random;

public class OTPGenerator_y {
  private static final Random rand = new Random();

    public static String generateOTP() {
    
        int otp = 100000 + rand.nextInt(900000);
        return String.valueOf(otp);
    }
}
