package admain;

public interface BookingRuleStrategy {
    boolean isValid(Appointment appointment);
}