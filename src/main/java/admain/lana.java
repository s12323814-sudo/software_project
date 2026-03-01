package admain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class lana {

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService();
    }

    @Test
    void testSuccessfulBooking() {
        TimeSlot slot = new TimeSlot(
            LocalDateTime.of(2026, 3, 1, 10, 0),
            LocalDateTime.of(2026, 3, 1, 11, 0)
        );
        Appointment appointment = new Appointment(slot, 3, "In-Person");

        boolean result = service.bookAppointment(appointment);
        assertTrue(result, "The appointment should be booked successfully");
    }

    @Test
    void testDuplicateBooking() {
        TimeSlot slot = new TimeSlot(
            LocalDateTime.of(2026, 3, 1, 10, 0),
            LocalDateTime.of(2026, 3, 1, 11, 0)
        );
        Appointment appt1 = new Appointment(slot, 3, "In-Person");
        service.bookAppointment(appt1);

        Appointment appt2 = new Appointment(slot, 2, "Virtual");
        boolean result = service.bookAppointment(appt2);
        assertFalse(result, "The slot is already booked, should return false");
    }

    @Test
    void testCapacityRule() {
        TimeSlot slot = new TimeSlot(
            LocalDateTime.of(2026, 3, 1, 12, 0),
            LocalDateTime.of(2026, 3, 1, 13, 0)
        );
        Appointment appointment = new Appointment(slot, 6, "In-Person"); // exceeds capacity

        boolean result = service.bookAppointment(appointment);
        assertFalse(result, "Exceeds capacity, should return false");
    }

    @Test
    void testDurationRule() {
        TimeSlot slot = new TimeSlot(
            LocalDateTime.of(2026, 3, 1, 14, 0),
            LocalDateTime.of(2026, 3, 1, 17, 0) // 3 hours > max 2
        );
        Appointment appointment = new Appointment(slot, 3, "In-Person");

        boolean result = service.bookAppointment(appointment);
        assertFalse(result, "Exceeds duration, should return false");
    }
}