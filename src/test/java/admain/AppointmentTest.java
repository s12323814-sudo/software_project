package admain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;

public class AppointmentTest {
@Test
void testEmptyConstructor() {
    Appointment appt = new Appointment();
    assertEquals(0, appt.getAppointmentId());
    assertEquals(0, appt.getUserId());
    assertNull(appt.getTimeSlot());
}

@Test
void testGetUsername_null() {
    Appointment appt = new Appointment();
    assertNull(appt.getUsername());
}

@Test
void testSetAndGetUsername() {
    Appointment appt = new Appointment();
    appt.setUsername("yasmeen");
    assertEquals("yasmeen", appt.getUsername());
}
    @Test
    public void testToString_withTimeSlot() {
        TimeSlot slot = new TimeSlot(1, 
                ZonedDateTime.parse("2025-01-01T10:00:00Z"), 
                ZonedDateTime.parse("2025-01-01T11:00:00Z"));
        Appointment appt = new Appointment(
                100, 10, 1, slot, 3, 
                AppointmentStatus_y.CONFIRMED, 
                AppointmentType_y.ONLINE);

        String str = appt.toString();

        assertTrue(str.contains("id=100"));
        assertTrue(str.contains("userId=10"));
        assertTrue(str.contains("slotId=1"));
        assertTrue(str.contains("participants=3"));
        assertTrue(str.contains("status=CONFIRMED"));
        assertTrue(str.contains("type=ONLINE"));
        assertTrue(str.contains("timeSlot=")); // تحقق من وجود TimeSlot
    }

    @Test
    public void testToString_withoutTimeSlot() {
        Appointment appt = new Appointment(
                101, 20, 2, 5, 
                AppointmentStatus_y.CANCELLED, 
                AppointmentType_y.GROUP);

        String str = appt.toString();

        assertTrue(str.contains("id=101"));
        assertTrue(str.contains("userId=20"));
        assertTrue(str.contains("slotId=2"));
        assertTrue(str.contains("participants=5"));
        assertTrue(str.contains("status=CANCELLED"));
        assertTrue(str.contains("type=GROUP"));
        assertFalse(str.contains("timeSlot="));
    }

    @Test
    public void testConstructorAndGetters() {

        Appointment appt = new Appointment(
                1,   // appointmentId
                10,  // userId
                5,   // slotId
                3,   // participants
                AppointmentStatus_y.CONFIRMED,
                AppointmentType_y.ONLINE
        );

        assertEquals(1, appt.getAppointmentId());
        assertEquals(10, appt.getUserId());
        assertEquals(5, appt.getSlotId());
        assertEquals(3, appt.getParticipants());
        assertEquals(AppointmentStatus_y.CONFIRMED, appt.getStatus());
        assertEquals(AppointmentType_y.ONLINE, appt.getType());
    }
    @Test
    public void testToString() {

        Appointment appt = new Appointment(
                1,
                10,
                5,
                3,
                AppointmentStatus_y.CONFIRMED,
                AppointmentType_y.ONLINE
        );

        String result = appt.toString();

        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("userId=10"));
        assertTrue(result.contains("slotId=5"));
        assertTrue(result.contains("participants=3"));
        assertTrue(result.contains("CONFIRMED"));
        assertTrue(result.contains("ONLINE"));
    }
    @Test
    public void testSetters() {

        Appointment appt = new Appointment(
                1, 10, 5, 2,
                AppointmentStatus_y.PENDING,
                AppointmentType_y.OFFLINE
        );

        appt.setParticipants(6);
        appt.setStatus(AppointmentStatus_y.CONFIRMED);

        assertEquals(6, appt.getParticipants());
        assertEquals(AppointmentStatus_y.CONFIRMED, appt.getStatus());
    }
    @Test
    public void testGettersAndSetters() {
        Appointment appt = new Appointment(
                102, 30, 3, 1, 
                AppointmentStatus_y.PENDING, 
                AppointmentType_y.INDIVIDUAL);

        appt.setParticipants(2);
        appt.setStatus(AppointmentStatus_y.CONFIRMED);
        TimeSlot slot = new TimeSlot(3, ZonedDateTime.now(), ZonedDateTime.now().plusHours(1));
        appt.setTimeSlot(slot);

        assertEquals(102, appt.getAppointmentId());
        assertEquals(30, appt.getUserId());
        assertEquals(3, appt.getSlotId());
        assertEquals(2, appt.getParticipants());
        assertEquals(AppointmentStatus_y.CONFIRMED, appt.getStatus());
        assertEquals(AppointmentType_y.INDIVIDUAL, appt.getType());
        assertEquals(slot, appt.getTimeSlot());
    }
}
