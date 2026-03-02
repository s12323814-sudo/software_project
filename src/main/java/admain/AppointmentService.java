package admain;

import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private ScheduleRepository repository = new ScheduleRepository();
    private List<BookingRuleStrategy> rules = new ArrayList<>();

    public AppointmentService() {
        rules.add(new DurationRule(2));
        rules.add(new CapacityRule(5));
    }

    public boolean bookAppointment(Appointment appointment) {
        if (!repository.isSlotAvailable(appointment.getTimeSlot())) {
            System.out.println("This slot is already booked!");
            return false;
        }

        for (BookingRuleStrategy rule : rules) {
            if (!rule.isValid(appointment)) {
                System.out.println("Appointment violates booking rules!");
                return false;
            }
        }

        repository.addAppointment(appointment);
        System.out.println("Appointment booked successfully!");
        return true;
    }

    public List<Appointment> getAppointments() {
        return repository.getAppointments();
    }
}