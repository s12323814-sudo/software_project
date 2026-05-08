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
void testGenerateOTP_fullCoverage() {
    String otp = OTPGenerator_y.generateOTP();

    assertNotNull(otp);
    assertEquals(6, otp.length());
    assertTrue(otp.matches("\\d{6}"));
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
        String otp = OTPGenerator_y.generateOTP();

        int value = Integer.parseInt(otp);

        assertTrue(value >= 100000 && value <= 999999);
    }
}
