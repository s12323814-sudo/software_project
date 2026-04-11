package admain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentSlot_yTest {

    @Test
    void testConstructorAndGetters() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 1, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                2
        );

        assertEquals(1, slot.getId());
        assertEquals(LocalDate.of(2026, 1, 1), slot.getDate());
        assertEquals(LocalTime.of(10, 0), slot.getStartTime());
        assertEquals(LocalTime.of(11, 0), slot.getEndTime());
        assertEquals(5, slot.getMaxCapacity());
        assertEquals(2, slot.getBookedCount());
    }

    @Test
    void testIsFull_false() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                3
        );

        assertFalse(slot.isFull());
    }

    @Test
    void testIsFull_true() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                5
        );

        assertTrue(slot.isFull());
    }

    @Test
    void testGetStartDateTime_notNull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 2, 2),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                5,
                1
        );

        assertNotNull(slot.getStartDateTime());
    }

    @Test
    void testGetEndDateTime_afterStart() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 2, 2),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                5,
                1
        );

        assertTrue(slot.getEndDateTime().isAfter(slot.getStartDateTime()));
    }

    @Test
    void testToString_containsData() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                10,
                LocalDate.of(2026, 3, 3),
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                10,
                4
        );

        String result = slot.toString();

        assertTrue(result.contains("10"));
        assertTrue(result.contains("2026-03-03"));
        assertTrue(result.contains("08:00"));
    }

    @Test
    void testSlotAvailableForResource_exceptionPath() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                0
        );

        // بما أنه DB مش موجود غالباً → رح يرمي Exception
        assertThrows(RuntimeException.class, () -> {
            slot.isSlotAvailableForResource(1, 1);
        });
    }
}