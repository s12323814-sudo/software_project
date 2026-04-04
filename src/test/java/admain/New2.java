package admain;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class New2{

    @Test
    void testGettersAndDuration() {

        ZonedDateTime start = ZonedDateTime.of(
                2026, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC")
        );

        ZonedDateTime end = ZonedDateTime.of(
                2026, 1, 1, 11, 30, 0, 0, ZoneId.of("UTC")
        );

        TimeSlot slot = new TimeSlot(1, start, end);

        assertEquals(1, slot.getId());
        assertEquals(start, slot.getStart());
        assertEquals(end, slot.getEnd());

        // duration = 90 minutes
        assertEquals(90, slot.getDurationMinutes());
    }

    @Test
    void testToString_containsAllFields() {

        ZonedDateTime start = ZonedDateTime.of(
                2026, 1, 1, 9, 0, 0, 0, ZoneId.of("UTC")
        );

        ZonedDateTime end = ZonedDateTime.of(
                2026, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC")
        );

        TimeSlot slot = new TimeSlot(2, start, end);

        String result = slot.toString();

        assertTrue(result.contains("id=2"));
        assertTrue(result.contains("start="));
        assertTrue(result.contains("end="));
        assertTrue(result.contains("duration=60 minutes"));
    }

    @Test
    void testZeroDuration() {

        ZonedDateTime time = ZonedDateTime.of(
                2026, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC")
        );

        TimeSlot slot = new TimeSlot(3, time, time);

        assertEquals(0, slot.getDurationMinutes());
    }
}
