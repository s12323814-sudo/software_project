package admain;

import org.junit.jupiter.api.*;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

public class TimeMockTest {

    @Test
    void testMockedTime() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-01-01T10:00:00Z"),
                ZoneId.of("Asia/Hebron")
        );

        LocalDateTime now = LocalDateTime.now(fixedClock);

        assertEquals(2026, now.getYear());
        assertEquals(1, now.getMonthValue());
    }
}