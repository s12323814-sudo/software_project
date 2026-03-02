package admain;

import java.time.Duration;

public class DurationRule implements BookingRuleStrategy {
    private final long maxHours;

    public DurationRule(long maxHours) { this.maxHours = maxHours; }

    @Override
    public boolean isValid(Appointment appointment) {
        Duration duration = Duration.between(
            appointment.getTimeSlot().getStart(),
            appointment.getTimeSlot().getEnd()
        );
        return duration.toHours() <= maxHours;
    }
}