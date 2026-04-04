package admain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AA {

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
}