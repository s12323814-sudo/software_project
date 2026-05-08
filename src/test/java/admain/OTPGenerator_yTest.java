package admain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OTPGeneratorTest {

    @Test
    void testGenerateOTP_shouldReturn6DigitNumber() {
        String otp = OTPGenerator_y.generateOTP();

        assertNotNull(otp);
        assertEquals(6, otp.length());
        int value = Integer.parseInt(otp);
        assertTrue(value >= 100000 && value <= 999999);
    }
}
