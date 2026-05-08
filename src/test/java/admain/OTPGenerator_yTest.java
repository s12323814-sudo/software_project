package admain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OTPGenerator_yTest {

    @Test
    void testGenerateOTP() {
        String otp = OTPGenerator_y.generateOTP();

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));

        int value = Integer.parseInt(otp);
        assertTrue(value >= 100000 && value <= 999999);
    }
}
