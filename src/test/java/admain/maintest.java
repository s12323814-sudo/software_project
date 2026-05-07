package admain;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;

class maintest {
	 @Test
	    void testRegisterMenu_success() {

	        authService_y authMock = mock(authService_y.class);

	        Account_y fakeAccount =
	                new Account_y(1, "yara", "pass123", "yara@test.com", Role_y.USER);

	        when(authMock.register(any(), any(), any(), any()))
	                .thenReturn(fakeAccount);

	        Account_y result =
	                authMock.register("yara", "pass123", "yara@test.com", Role_y.USER);

	        assertNotNull(result);
	        assertEquals("yara", result.getUsername());
	    }
    @Test
    void testViewUserAppointments_returnsData() throws SQLException {

        SlotService_y mockService = mock(SlotService_y.class);

        Account_y user = new Account_y(1, "yara", "pass123", "yara@test.com", Role_y.USER);

        Appointment a1 = mock(Appointment.class);
        Appointment a2 = mock(Appointment.class);

        when(a1.toString()).thenReturn("A1");
        when(a2.toString()).thenReturn("A2");

        when(mockService.viewUserAppointments(1))
                .thenReturn(List.of(a1, a2));

        List<Appointment> result = mockService.viewUserAppointments(1);

        assertEquals(2, result.size());
        assertEquals("A1", result.get(0).toString());
    }
    @Test
    void testForgotPassword_emailExistsAndSendOTP() {

        authService_y authMock = mock(authService_y.class);
        EmailService_y emailMock = mock(EmailService_y.class);

        when(authMock.emailExists("test@email.com")).thenReturn(true);

        String otp = "123456";

        // simulate sendOTP
        doNothing().when(emailMock).sendOTP(any(), any());

        emailMock.sendOTP("test@email.com", otp);

        verify(emailMock, times(1))
                .sendOTP(eq("test@email.com"), eq(otp));
    }
    @Test
    void testBookAppointment_success() throws Exception {

        SlotService_y slotServiceMock = mock(SlotService_y.class);

        when(slotServiceMock.bookAppointment(anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(true);

        boolean result = slotServiceMock.bookAppointment(
                1, 10, 2, AppointmentType_y.GROUP
        );

        assertTrue(result);
    }
}