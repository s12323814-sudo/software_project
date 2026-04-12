package admain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OTPGenerator_yTest {

    @Test
    void testGenerateOTP_notNull() {

        String otp = OTPGenerator_y.generateOTP();

        assertNotNull(otp);
    }

    @Test
    void testGenerateOTP_lengthIs6() {

        String otp = OTPGenerator_y.generateOTP();

        assertEquals(6, otp.length());
    }

    @Test
    void testGenerateOTP_isNumeric() {

        String otp = OTPGenerator_y.generateOTP();

        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void testGenerateOTP_range() {

        int otp = Integer.parseInt(OTPGenerator_y.generateOTP());

        assertTrue(otp >= 100000 && otp <= 999999);
    }
}