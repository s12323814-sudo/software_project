package admain;

public class CapacityRule implements BookingRuleStrategy {
    private final int maxParticipants;

    public CapacityRule(int maxParticipants) { this.maxParticipants = maxParticipants; }

    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getParticipants() <= maxParticipants;
    }
}