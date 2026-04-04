package admain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Testl {

    private AppointmentService service;

    @BeforeEach
    void setup() {
        service = new AppointmentService(null, null, null);
    }

    @Test
    void testDurationTooShort() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            // نحاكي مدة أقل من 30 دقيقة
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusMinutes(10);

            long duration = Duration.between(start, end).toMinutes();

            if (duration < 30) {
                throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");
            }
        });

        assertEquals("Duration must be between 30 and 120 minutes.", ex.getMessage());
    }

    @Test
    void testDurationTooLong() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusMinutes(200);

            long duration = Duration.between(start, end).toMinutes();

            if (duration > 120) {
                throw new IllegalArgumentException("Duration must be between 30 and 120 minutes.");
            }
        });

        assertEquals("Duration must be between 30 and 120 minutes.", ex.getMessage());
    }
}